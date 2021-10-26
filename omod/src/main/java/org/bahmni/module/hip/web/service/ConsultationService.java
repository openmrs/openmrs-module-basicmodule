package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.ConsultationDao;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.bahmni.module.hip.web.model.OpenMrsCondition;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConsultationService {

    private final ConsultationDao consultationDao;
    private final OPConsultDao opConsultDao;
    private final DiagnosticReportService diagnosticReportService;
    public static Set<String> conceptNames = new HashSet<>(Arrays.asList("Tuberculosis, Treatment Plan","Tuberculosis, Next Followup Visit","Tuberculosis, Plan for next visit","Tuberculosis, Patient Category","Current Followup Visit After",
            "Tuberculosis, Plan for next visit","Malaria, Parents Name","Malaria, Death Date", "Childhood Illness, Vitamin A Capsules Provided","Childhood Illness, Albendazole Given","Childhood Illness, Referred out",
            "Childhood Illness, Vitamin A Capsules Provided","Childhood Illness, Albendazole Given","Nutrition, Bal Vita Provided by FCHV","Bal Vita Provided by FCHV","ART, Condoms given","HIVTC, Marital Status","Malaria, Contact number",
            "HIVTC, Transferred out", "HIVTC, Regimen when transferred out", "HIVTC, Date of transferred out", "HIVTC, Transferred out to", "HIVTC, Chief Complaint"));

    @Autowired
    public ConsultationService(ConsultationDao consultationDao, OPConsultDao opConsultDao, DiagnosticReportService diagnosticReportService) {
        this.consultationDao = consultationDao;
        this.opConsultDao = opConsultDao;
        this.diagnosticReportService = diagnosticReportService;
    }

    public ConcurrentHashMap<Encounter, List<OpenMrsCondition>> getEncounterChiefComplaintsMap(Patient patient, String visitType, Date fromDate, Date toDate) {
        List<Obs> chiefComplaints = consultationDao.getChiefComplaints(patient, visitType, fromDate, toDate);
        ConcurrentHashMap<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = new ConcurrentHashMap<>();

        for (Obs o : chiefComplaints) {
            if(!encounterChiefComplaintsMap.containsKey(o.getEncounter())){
                encounterChiefComplaintsMap.put(o.getEncounter(), new ArrayList<>());
            }
            encounterChiefComplaintsMap.get(o.getEncounter()).add(new OpenMrsCondition(o.getUuid(), o.getValueCoded().getDisplayString(), o.getDateCreated()));
        }
        return encounterChiefComplaintsMap;
    }

    public Map<Encounter, List<OpenMrsCondition>> getEncounterMedicalHistoryConditionsMap(Patient patient, String visit, Date fromDate, Date toDate) {
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

    public Map<Encounter, List<Obs>> getEncounterPhysicalExaminationMap(Patient patient, String visitType, Date fromDate, Date toDate) {
        List<Obs> physicalExaminations = consultationDao.getPhysicalExamination(patient, visitType, fromDate, toDate);
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

    public Map<Encounter, List<Obs>> getEncounterPatientDocumentsMap(String visitType, Date fromDate, Date toDate, Patient patient) {
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

    public Map<Encounter, List<Order>> getEncounterOrdersMap(String visitType, Date fromDate, Date toDate, Patient patient) {
        List<Order> orders = consultationDao.getOrders(patient, visitType, fromDate, toDate);
        Map<Encounter, List<Order>> encounterOrdersMap = new HashMap<>();
        for(Order order : orders){
            if (!encounterOrdersMap.containsKey(order.getEncounter())) {
                encounterOrdersMap.put(order.getEncounter(), new ArrayList<>());
            }
            encounterOrdersMap.get(order.getEncounter()).add(order);
        }
        return encounterOrdersMap;
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
}
