package org.bahmni.module.hip.web.model;

import lombok.Getter;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.EncounterProvider;
import org.openmrs.DrugOrder;
import org.openmrs.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class OpenMrsOPConsult {
    private final Encounter encounter;
    private final List<OpenMrsCondition> chiefComplaintConditions;
    private final List<OpenMrsCondition> medicalHistoryConditions;
    private final List<Obs> observations;
    private final Patient patient;
    private final Set<EncounterProvider> encounterProviders;
    private final List<DrugOrder> drugOrders;
    private final Obs procedure;
    private final List<Obs> patientDocuments;
    private final List<Order> orders;

    public OpenMrsOPConsult(Encounter encounter,
                            List<OpenMrsCondition> chiefComplaintConditions,
                            List<OpenMrsCondition> medicalHistoryConditions,
                            List<Obs> observations,
                            Patient patient,
                            Set<EncounterProvider> encounterProviders,
                            List<DrugOrder> drugOrders,
                            Obs procedure,
                            List<Obs> patientDocuments,
                            List<Order> orders) {
        this.encounter = encounter;
        this.chiefComplaintConditions = chiefComplaintConditions;
        this.medicalHistoryConditions = medicalHistoryConditions;
        this.observations = observations;
        this.patient = patient;
        this.encounterProviders = encounterProviders;
        this.drugOrders = drugOrders;
        this.procedure = procedure;
        this.patientDocuments = patientDocuments;
        this.orders = orders;
    }

    public static List<OpenMrsOPConsult> getOpenMrsOPConsultList(Map<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap,
                                                                 Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap,
                                                                 Map<Encounter, List<Obs>> encounterPhysicalExaminationMap,
                                                                 Map<Encounter, DrugOrders> encounteredDrugOrdersMap,
                                                                 Map<Encounter, Obs> encounterProcedureMap,
                                                                 Map<Encounter, List<Obs>> encounterPatientDocumentsMap,
                                                                 Map<Encounter, List<Order>> encounterOrdersMap,
                                                                 Patient patient) {
        List<OpenMrsOPConsult> openMrsOPConsultList = new ArrayList<>();

        for(Map.Entry<Encounter, DrugOrders> entry : encounteredDrugOrdersMap.entrySet()){
            List<DrugOrder> drugOrdersList = encounteredDrugOrdersMap.get(entry.getKey()).getOpenMRSDrugOrders();

            List<Obs> patientDocumentsList = getEncounterObs(encounterPatientDocumentsMap, entry.getKey());
            patientDocumentsList = patientDocumentsList == null ? new ArrayList<>() : patientDocumentsList;

            List<OpenMrsCondition> medicalHistoryList = getEncounterConditions(encounterMedicalHistoryMap, entry.getKey());
            medicalHistoryList = medicalHistoryList == null ? new ArrayList<>() : medicalHistoryList;

            List<Obs> physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, entry.getKey());
            physicalExaminationList = physicalExaminationList == null ? new ArrayList<>() : physicalExaminationList;

            List<OpenMrsCondition> chiefComplaintList = getEncounterConditions(encounterChiefComplaintsMap, entry.getKey());
            chiefComplaintList = chiefComplaintList == null ? new ArrayList<>() : chiefComplaintList;

            Obs procedure = getEncounterObsProcedure(encounterProcedureMap, entry.getKey());

            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), chiefComplaintList, medicalHistoryList, physicalExaminationList, patient, entry.getKey().getEncounterProviders(), drugOrdersList, procedure, patientDocumentsList, new ArrayList<>()));
        }

        for (Map.Entry<Encounter, List<OpenMrsCondition>> entry : encounterChiefComplaintsMap.entrySet()) {
            List<OpenMrsCondition> chiefComplaintList = encounterChiefComplaintsMap.get(entry.getKey());

            List<Obs> patientDocumentsList = getEncounterObs(encounterPatientDocumentsMap, entry.getKey());
            patientDocumentsList = patientDocumentsList == null ? new ArrayList<>() : patientDocumentsList;

            List<OpenMrsCondition> medicalHistoryList = getEncounterConditions(encounterMedicalHistoryMap, entry.getKey());
            medicalHistoryList = medicalHistoryList == null ? new ArrayList<>() : medicalHistoryList;

            List<Obs> physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, entry.getKey());
            physicalExaminationList = physicalExaminationList == null ? new ArrayList<>() : physicalExaminationList;

            Obs procedure = getEncounterObsProcedure(encounterProcedureMap, entry.getKey());

            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), chiefComplaintList, medicalHistoryList, physicalExaminationList, patient, entry.getKey().getEncounterProviders(), new ArrayList<>(), procedure, patientDocumentsList, new ArrayList<>()));
        }

        for (Map.Entry<Encounter, List<OpenMrsCondition>> entry : encounterMedicalHistoryMap.entrySet()) {
            List<OpenMrsCondition> medicalHistoryList = encounterMedicalHistoryMap.get(entry.getKey());

            List<Obs> patientDocumentsList = getEncounterObs(encounterPatientDocumentsMap, entry.getKey());
            patientDocumentsList = patientDocumentsList == null ? new ArrayList<>() : patientDocumentsList;

            List<Obs> physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, entry.getKey());
            physicalExaminationList = physicalExaminationList == null ? new ArrayList<>() : physicalExaminationList;

            Obs procedure = getEncounterObsProcedure(encounterProcedureMap, entry.getKey());
            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), new ArrayList<>(), medicalHistoryList, physicalExaminationList, patient, entry.getKey().getEncounterProviders(), new ArrayList<>(), procedure, patientDocumentsList, new ArrayList<>()));
        }

        for (Map.Entry<Encounter, List<Obs>> entry : encounterPhysicalExaminationMap.entrySet()) {
            List<Obs> physicalExaminationList = encounterPhysicalExaminationMap.get(entry.getKey());

            List<Obs> patientDocumentsList = getEncounterObs(encounterPatientDocumentsMap, entry.getKey());
            patientDocumentsList = patientDocumentsList == null ? new ArrayList<>() : patientDocumentsList;

            Obs procedure = getEncounterObsProcedure(encounterProcedureMap, entry.getKey());

            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), new ArrayList<>(), new ArrayList<>(), physicalExaminationList, patient, entry.getKey().getEncounterProviders(), new ArrayList<>(), procedure, patientDocumentsList, new ArrayList<>()));
        }

        for (Map.Entry<Encounter, Obs> entry : encounterProcedureMap.entrySet()) {
            List<Obs> patientDocumentsList = getEncounterObs(encounterPatientDocumentsMap, entry.getKey());
            patientDocumentsList = patientDocumentsList == null ? new ArrayList<>() : patientDocumentsList;

            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), patient, entry.getKey().getEncounterProviders(), new ArrayList<>(), entry.getValue(), patientDocumentsList, new ArrayList<>()));
        }

        for (Map.Entry<Encounter, List<Obs>> entry : encounterPatientDocumentsMap.entrySet()) {
            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), patient, entry.getKey().getEncounterProviders(), new ArrayList<>(), null, encounterPatientDocumentsMap.get(entry.getKey()), new ArrayList<>()));
        }

        for(Map.Entry<Encounter, List<Order>> entry : encounterOrdersMap.entrySet()){
            openMrsOPConsultList.add(new OpenMrsOPConsult(entry.getKey(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), patient, entry.getKey().getEncounterProviders(), new ArrayList<>(), null, new ArrayList<>(), encounterOrdersMap.get(entry.getKey())));
        }

        return openMrsOPConsultList;
    }

    public static List<Obs> getEncounterObs(Map<Encounter, List<Obs>> map, Encounter encounter) {
        if (map.containsKey(encounter)) {
            List<Obs> obsList = map.get(encounter);
            map.remove(encounter);
            return obsList;
        }
        return null;
    }

    public static List<OpenMrsCondition> getEncounterConditions(Map<Encounter, List<OpenMrsCondition>> map, Encounter encounter) {
        if (map.containsKey(encounter)) {
            List<OpenMrsCondition> conditionList = map.get(encounter);
            map.remove(encounter);
            return conditionList;
        }
        return null;
    }

    public static Obs getEncounterObsProcedure(Map<Encounter, Obs> map, Encounter encounter) {
        if (map.containsKey(encounter)) {
            Obs obs = map.get(encounter);
            map.remove(encounter);
            return obs;
        }
        return null;
    }
}
