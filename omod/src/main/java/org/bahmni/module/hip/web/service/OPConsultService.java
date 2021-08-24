package org.bahmni.module.hip.web.service;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.bahmni.module.hip.web.model.OPConsultBundle;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.OpenMrsCondition;
import org.bahmni.module.hip.web.model.DrugOrders;
import org.bahmni.module.hip.web.model.OpenMrsOPConsult;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OPConsultService {
    public static Set<String> conceptNames = new HashSet<>(Arrays.asList("Tuberculosis, Treatment Plan","Tuberculosis, Next Followup Visit","Tuberculosis, Plan for next visit","Tuberculosis, Patient Category","Current Followup Visit After",
            "Tuberculosis, Plan for next visit","Malaria, Parents Name","Malaria, Death Date", "Childhood Illness, Vitamin A Capsules Provided","Childhood Illness, Albendazole Given","Childhood Illness, Referred out",
            "Childhood Illness, Vitamin A Capsules Provided","Childhood Illness, Albendazole Given","Nutrition, Bal Vita Provided by FCHV","Bal Vita Provided by FCHV","ART, Condoms given","HIVTC, Marital Status","Malaria, Contact number",
            "HIVTC, Transferred out", "HIVTC, Regimen when transferred out", "HIVTC, Date of transferred out", "HIVTC, Transferred out to", "HIVTC, Chief Complaint"));

    private final FhirBundledOPConsultBuilder fhirBundledOPConsultBuilder;
    private final OPConsultDao opConsultDao;
    private final PatientService patientService;
    private final OpenMRSDrugOrderClient openMRSDrugOrderClient;
    private final DiagnosticReportService diagnosticReportService;

    @Autowired
    public OPConsultService(FhirBundledOPConsultBuilder fhirBundledOPConsultBuilder, OPConsultDao opConsultDao,
                            PatientService patientService, OpenMRSDrugOrderClient openMRSDrugOrderClient,
                            DiagnosticReportService diagnosticReportService) {
        this.fhirBundledOPConsultBuilder = fhirBundledOPConsultBuilder;
        this.opConsultDao = opConsultDao;
        this.patientService = patientService;
        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
        this.diagnosticReportService = diagnosticReportService;
    }

    public List<OPConsultBundle> getOpConsultsForVisit(String patientUuid, DateRange dateRange, String visitType) {
        Date fromDate = dateRange.getFrom();
        Date toDate = dateRange.getTo();
        Patient patient = patientService.getPatientByUuid(patientUuid);

        Map<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = getEncounterChiefComplaintsMap(patient, visitType, fromDate, toDate);
        Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap = getEncounterMedicalHistoryConditionsMap(patient, visitType, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPhysicalExaminationMap = getEncounterPhysicalExaminationMap(patient, visitType, fromDate, toDate);
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndVisitTypeFor(patientUuid, dateRange, visitType));
        Map<Encounter, DrugOrders> encounteredDrugOrdersMap = drugOrders.groupByEncounter();
        Map<Encounter, Obs> encounterProcedureMap = getEncounterProcedureMap(patient, visitType, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPatientDocumentsMap = getEncounterPatientDocumentsMap(visitType, fromDate, toDate, patient);

        List<OpenMrsOPConsult> openMrsOPConsultList = OpenMrsOPConsult.getOpenMrsOPConsultList(encounterChiefComplaintsMap,
                encounterMedicalHistoryMap, encounterPhysicalExaminationMap, encounteredDrugOrdersMap, encounterProcedureMap,
                encounterPatientDocumentsMap, patient);

        return openMrsOPConsultList.stream().
                map(fhirBundledOPConsultBuilder::fhirBundleResponseFor).collect(Collectors.toList());
    }

    private Map<Encounter, List<Obs>> getEncounterPatientDocumentsMap(String visitType, Date fromDate, Date toDate, Patient patient) {
        final int patientDocumentEncounterType = 9;
        Map<Encounter, List<Obs>> encounterDiagnosticReportsMap = diagnosticReportService.getAllObservationsForVisits(fromDate, toDate, patient, visitType);
        Map<Encounter, List<Obs>> encounterPatientDocumentsMap = new HashMap<>();
        for (Encounter e : encounterDiagnosticReportsMap.keySet()) {
            List<Obs> patientDocuments = e.getAllObs().stream().
                    filter(o -> (o.getEncounter().getEncounterType().getEncounterTypeId() == patientDocumentEncounterType && o.getValueText() == null))
                    .collect(Collectors.toList());
            if (patientDocuments.size() > 0) {
                encounterPatientDocumentsMap.put(e, patientDocuments);
            }
        }
        return encounterPatientDocumentsMap;
    }

    private Map<Encounter, Obs> getEncounterProcedureMap(Patient patient, String visitType, Date fromDate, Date toDate) {
        List<Obs> obsProcedures = opConsultDao.getProcedures(patient, visitType, fromDate, toDate);
        Map<Encounter, Obs> encounterProcedureMap = new HashMap<>();
        for(Obs o: obsProcedures){
            encounterProcedureMap.put(o.getEncounter(), o);
        }
        return encounterProcedureMap;
    }

    private Map<Encounter, List<Obs>> getEncounterPhysicalExaminationMap(Patient patient, String visitType, Date fromDate, Date toDate) {
        List<Obs> physicalExaminations = opConsultDao.getPhysicalExamination(patient, visitType, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPhysicalExaminationMap = new HashMap<>();
        for (Obs physicalExamination : physicalExaminations) {
            Encounter encounter = physicalExamination.getEncounter();
            List<Obs> groupMembers = new ArrayList<>();
            getGroupMembersOfObs(physicalExamination, groupMembers);
            if (!encounterPhysicalExaminationMap.containsKey(encounter)) {
                encounterPhysicalExaminationMap.put(encounter, new ArrayList<>());
            }
            encounterPhysicalExaminationMap.get(encounter).addAll(groupMembers);
        }
        return encounterPhysicalExaminationMap;
    }

    private void getGroupMembersOfObs(Obs physicalExamination, List<Obs> groupMembers) {
        if (physicalExamination.getGroupMembers().size() > 0) {
            for (Obs groupMember : physicalExamination.getGroupMembers()) {
                if (conceptNames.contains(groupMember.getConcept().getDisplayString())) continue;
                getGroupMembersOfObs(groupMember, groupMembers);
            }
        } else {
            groupMembers.add(physicalExamination);
        }
    }

    private Map<Encounter, List<OpenMrsCondition>> getEncounterChiefComplaintsMap(Patient patient, String visitType, Date fromDate, Date toDate) {
        List<Obs> chiefComplaints = opConsultDao.getChiefComplaints(patient, visitType, fromDate, toDate);
        HashMap<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = new HashMap<>();

        for (Obs o : chiefComplaints) {
            if(!encounterChiefComplaintsMap.containsKey(o.getEncounter())){
                encounterChiefComplaintsMap.put(o.getEncounter(), new ArrayList<>());
            }
            encounterChiefComplaintsMap.get(o.getEncounter()).add(new OpenMrsCondition(o.getUuid(), o.getValueCoded().getDisplayString(), o.getDateCreated()));
        }
        return encounterChiefComplaintsMap;
    }

    private Map<Encounter, List<OpenMrsCondition>> getEncounterMedicalHistoryConditionsMap(Patient patient, String visit, Date fromDate, Date toDate) {
        Map<Encounter, List<Condition>> medicalHistoryConditionsMap =  opConsultDao.getMedicalHistoryConditions(patient, visit, fromDate, toDate);
        List<Obs> medicalHistoryDiagnosisMap =  opConsultDao.getMedicalHistoryDiagnosis(patient, visit, fromDate, toDate);
        Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap = new HashMap<>();

        for(Map.Entry<Encounter, List<Condition>> medicalHistory : medicalHistoryConditionsMap.entrySet()){
            if (!encounterMedicalHistoryMap.containsKey(medicalHistory.getKey())){
                encounterMedicalHistoryMap.put(medicalHistory.getKey(), new ArrayList<>());
            }
            for(Condition condition : medicalHistory.getValue()){
                encounterMedicalHistoryMap.get(medicalHistory.getKey()).add(new OpenMrsCondition(condition.getUuid(), condition.getConcept().getDisplayString(), condition.getDateCreated()));
            }
        }
        for(Obs obs : medicalHistoryDiagnosisMap){
            if (!encounterMedicalHistoryMap.containsKey(obs.getEncounter())){
                encounterMedicalHistoryMap.put(obs.getEncounter(), new ArrayList<>());
            }
            encounterMedicalHistoryMap.get(obs.getEncounter()).add(new OpenMrsCondition(obs.getUuid(), obs.getValueCoded().getDisplayString(), obs.getDateCreated()));
        }
        return encounterMedicalHistoryMap;
    }
}
