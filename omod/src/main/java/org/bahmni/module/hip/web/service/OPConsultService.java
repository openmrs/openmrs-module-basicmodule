package org.bahmni.module.hip.web.service;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.bahmni.module.hip.web.model.OPConsultBundle;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.OpenMrsCondition;
import org.bahmni.module.hip.web.model.DrugOrders;
import org.bahmni.module.hip.web.model.OpenMrsOPConsult;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class OPConsultService {

    private final FhirBundledOPConsultBuilder fhirBundledOPConsultBuilder;
    private final OPConsultDao opConsultDao;
    private final PatientService patientService;
    private final OpenMRSDrugOrderClient openMRSDrugOrderClient;
    private final ConsultationService consultationService;

    @Autowired
    public OPConsultService(FhirBundledOPConsultBuilder fhirBundledOPConsultBuilder,
                            OPConsultDao opConsultDao,
                            PatientService patientService,
                            OpenMRSDrugOrderClient openMRSDrugOrderClient,
                            ConsultationService consultationService) {
        this.fhirBundledOPConsultBuilder = fhirBundledOPConsultBuilder;
        this.opConsultDao = opConsultDao;
        this.patientService = patientService;
        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
        this.consultationService = consultationService;
    }

    public List<OPConsultBundle> getOpConsultsForVisit(String patientUuid, DateRange dateRange, String visitType) {
        Date fromDate = dateRange.getFrom();
        Date toDate = dateRange.getTo();
        Patient patient = patientService.getPatientByUuid(patientUuid);

        Map<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = consultationService.getEncounterChiefComplaintsMap(patient, visitType, fromDate, toDate);
        Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap = consultationService.getEncounterMedicalHistoryConditionsMap(patient, visitType, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPhysicalExaminationMap = consultationService.getEncounterPhysicalExaminationMap(patient, visitType, fromDate, toDate);
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndVisitTypeFor(patientUuid, dateRange, visitType));
        Map<Encounter, DrugOrders> encounteredDrugOrdersMap = drugOrders.groupByEncounter();
        Map<Encounter, Obs> encounterProcedureMap = getEncounterProcedureMap(patient, visitType, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPatientDocumentsMap = consultationService.getEncounterPatientDocumentsMap(visitType, fromDate, toDate, patient);
        Map<Encounter, List<Order>> encounterOrdersMap = consultationService.getEncounterOrdersMap(visitType, fromDate, toDate, patient);

        List<OpenMrsOPConsult> openMrsOPConsultList = OpenMrsOPConsult.getOpenMrsOPConsultList(encounterChiefComplaintsMap,
                encounterMedicalHistoryMap, encounterPhysicalExaminationMap, encounteredDrugOrdersMap, encounterProcedureMap,
                encounterPatientDocumentsMap, encounterOrdersMap, patient);

        return openMrsOPConsultList.stream().
                map(fhirBundledOPConsultBuilder::fhirBundleResponseFor).collect(Collectors.toList());
    }

    public List<OPConsultBundle> getOpConsultsForProgram(String patientUuid, DateRange dateRange, String programName,String programEnrollmentId) {
        Date fromDate = dateRange.getFrom();
        Date toDate = dateRange.getTo();
        Patient patient = patientService.getPatientByUuid(patientUuid);

        Map<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = consultationService. getEncounterChiefComplaintsMapForProgram(programName,fromDate,toDate,patient);
        Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap = consultationService.getEncounterMedicalHistoryConditionsMapForProgram(programName,fromDate,toDate,patient);
        Map<Encounter, List<Obs>> encounterPhysicalExaminationMap = consultationService.getEncounterPhysicalExaminationMapForProgram(programName,fromDate,toDate,patient);
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndProgramFor(patientUuid, dateRange,programName,programEnrollmentId));
        Map<Encounter, DrugOrders> encounteredDrugOrdersMap = drugOrders.groupByEncounter();
        Map<Encounter, Obs> encounterProcedureMap = getEncounterProcedureMapForProgram(programName,fromDate,toDate,patient);
        Map<Encounter, List<Obs>> encounterPatientDocumentsMap = consultationService.getEncounterPatientDocumentsMapForProgram(programName,fromDate,toDate,patient,programEnrollmentId);
        Map<Encounter, List<Order>> encounterOrdersMap = consultationService.getEncounterOrdersMapForProgram(programName,fromDate,toDate,patient);

        List<OpenMrsOPConsult> openMrsOPConsultList = OpenMrsOPConsult.getOpenMrsOPConsultList(encounterChiefComplaintsMap,
                encounterMedicalHistoryMap, encounterPhysicalExaminationMap, encounteredDrugOrdersMap, encounterProcedureMap,
                encounterPatientDocumentsMap, encounterOrdersMap, patient);

        return openMrsOPConsultList.stream().
                map(fhirBundledOPConsultBuilder::fhirBundleResponseFor).collect(Collectors.toList());
    }

    private Map<Encounter, Obs> getEncounterProcedureMap(Patient patient, String visitType, Date fromDate, Date toDate) {
        List<Obs> obsProcedures = opConsultDao.getProcedures(patient, visitType, fromDate, toDate);
        Map<Encounter, Obs> encounterProcedureMap = new HashMap<>();
        for(Obs o: obsProcedures){
            encounterProcedureMap.put(o.getEncounter(), o);
        }
        return encounterProcedureMap;
    }

    private Map<Encounter, Obs> getEncounterProcedureMapForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<Obs> obsProcedures = opConsultDao.getProceduresForProgram(programName,fromDate, toDate,patient);
        Map<Encounter, Obs> encounterProcedureMap = new HashMap<>();
        for(Obs o: obsProcedures){
            encounterProcedureMap.put(o.getEncounter(), o);
        }
        return encounterProcedureMap;
    }
}
