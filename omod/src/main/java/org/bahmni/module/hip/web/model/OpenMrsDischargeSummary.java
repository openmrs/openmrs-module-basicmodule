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
public class OpenMrsDischargeSummary {

    private final Encounter encounter;
    private final List<Obs> carePlanObs;
    private final Patient patient;
    private final Set<EncounterProvider> encounterProviders;
    private final List<DrugOrder> drugOrders;
    private final List<OpenMrsCondition> chiefComplaints;
    private final List<OpenMrsCondition> medicalHistory;
    private final List<Obs> physicalExaminationObs;
    private final List<Obs> patientDocuments;
    private final Obs procedure;
    private final List<Order> orders;

    public OpenMrsDischargeSummary(Encounter encounter,
                                   List<Obs> carePlanObs,
                                   List<DrugOrder> drugOrders,
                                   Patient patient,
                                   Set<EncounterProvider> encounterProviders,
                                   List<OpenMrsCondition> chiefComplaints,
                                   List<OpenMrsCondition> medicalHistory,
                                   List<Obs> patientDocuments,
                                   List<Obs> physicalExaminationObs,
                                   Obs procedure, List<Order> orders){
        this.encounter = encounter;
        this.carePlanObs = carePlanObs;
        this.drugOrders = drugOrders;
        this.patient = patient;
        this.encounterProviders = encounterProviders;
        this.chiefComplaints = chiefComplaints;
        this.medicalHistory = medicalHistory;
        this.physicalExaminationObs = physicalExaminationObs;
        this.patientDocuments = patientDocuments;
        this.procedure = procedure;
        this.orders = orders;
    }
    public static List<OpenMrsDischargeSummary> getOpenMrsDischargeSummaryList(Map<Encounter, List<Obs>> encounterCarePlanMap,
                                                                               Map<Encounter, DrugOrders> encounterDrugOrdersMap,
                                                                               Map<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap,
                                                                               Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap,
                                                                               Map<Encounter, List<Obs>> encounterPhysicalExaminationMap,
                                                                               Map<Encounter, List<Obs>> encounterPatientDocumentsMap,
                                                                               Map<Encounter, Obs> encounterProcedureNotesMap,
                                                                               Map<Encounter, List<Order>> encounterOrdersMap,
                                                                               Patient patient){
        List<OpenMrsDischargeSummary> openMrsDischargeSummaryList = new ArrayList<>();

        for(Map.Entry<Encounter, List<Obs>> encounterListEntry : encounterCarePlanMap.entrySet()){
            List<Obs> carePlanList = encounterCarePlanMap.get(encounterListEntry.getKey());
            List<DrugOrder> drugOrdersList = new ArrayList<>();
            List<OpenMrsCondition> chiefComplaintList = new ArrayList<>();
            List<OpenMrsCondition> medicalHistoryList = new ArrayList<>();
            List<Obs> physicalExaminationList = new ArrayList<>();
            List<Obs> patientDocumentList = new ArrayList<>();
            List<Order> orderList = new ArrayList<>();
            Obs procedure = null;
            if (encounterDrugOrdersMap.get(encounterListEntry.getKey()) != null){
                drugOrdersList = encounterDrugOrdersMap.get(encounterListEntry.getKey()).getOpenMRSDrugOrders();
                encounterDrugOrdersMap.remove(encounterListEntry.getKey());
            }
            if (encounterChiefComplaintsMap.get(encounterListEntry.getKey()) != null) {
                chiefComplaintList = getEncounterConditions(encounterChiefComplaintsMap, encounterListEntry.getKey());
                encounterChiefComplaintsMap.remove(encounterListEntry.getKey());
            }
            if(encounterMedicalHistoryMap.get(encounterListEntry.getKey()) != null) {
                medicalHistoryList = getEncounterConditions(encounterMedicalHistoryMap, encounterListEntry.getKey());
                encounterMedicalHistoryMap.remove(encounterListEntry.getKey());
            }
            if(encounterPhysicalExaminationMap.get(encounterListEntry.getKey()) != null){
                physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, encounterListEntry.getKey());
                encounterPhysicalExaminationMap.remove(encounterListEntry.getKey());
            }
            if(encounterPatientDocumentsMap.get(encounterListEntry.getKey()) != null){
                patientDocumentList = getEncounterObs(encounterPatientDocumentsMap, encounterListEntry.getKey());
                encounterPatientDocumentsMap.remove(encounterListEntry.getKey());
            }
            if(encounterProcedureNotesMap.get(encounterListEntry.getKey()) != null) {
                procedure = getEncounterObsProcedure(encounterProcedureNotesMap, encounterListEntry.getKey());
                encounterProcedureNotesMap.remove(encounterListEntry.getKey());
            }
            if(encounterOrdersMap.get(encounterListEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, encounterListEntry.getKey());
                encounterOrdersMap.remove(encounterListEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(encounterListEntry.getKey(), carePlanList, drugOrdersList, patient, encounterListEntry.getKey().getEncounterProviders(), chiefComplaintList, medicalHistoryList, patientDocumentList, physicalExaminationList, procedure, orderList));
        }

        for(Map.Entry<Encounter, DrugOrders> encounterListEntry : encounterDrugOrdersMap.entrySet()){
            List<DrugOrder> drugOrdersList = encounterDrugOrdersMap.get(encounterListEntry.getKey()).getOpenMRSDrugOrders();
            List<OpenMrsCondition> chiefComplaintList = new ArrayList<>();
            List<OpenMrsCondition> medicalHistoryList = new ArrayList<>();
            List<Obs> physicalExaminationList = new ArrayList<>();
            List<Obs> patientDocumentList = new ArrayList<>();
            List<Order> orderList = new ArrayList<>();
            Obs procedure = null;
            if (encounterChiefComplaintsMap.get(encounterListEntry.getKey()) != null) {
                chiefComplaintList = getEncounterConditions(encounterChiefComplaintsMap, encounterListEntry.getKey());
                encounterChiefComplaintsMap.remove(encounterListEntry.getKey());
            }
            if(encounterMedicalHistoryMap.get(encounterListEntry.getKey()) != null) {
                medicalHistoryList = getEncounterConditions(encounterMedicalHistoryMap, encounterListEntry.getKey());
                encounterMedicalHistoryMap.remove(encounterListEntry.getKey());
            }
            if(encounterPhysicalExaminationMap.get(encounterListEntry.getKey()) != null){
                physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, encounterListEntry.getKey());
                encounterPhysicalExaminationMap.remove(encounterListEntry.getKey());
            }
            if(encounterPatientDocumentsMap.get(encounterListEntry.getKey()) != null){
                patientDocumentList = getEncounterObs(encounterPatientDocumentsMap, encounterListEntry.getKey());
                encounterPatientDocumentsMap.remove(encounterListEntry.getKey());
            }
            if(encounterProcedureNotesMap.get(encounterListEntry.getKey()) != null) {
                procedure = getEncounterObsProcedure(encounterProcedureNotesMap, encounterListEntry.getKey());
                encounterProcedureNotesMap.remove(encounterListEntry.getKey());
            }
            if(encounterOrdersMap.get(encounterListEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, encounterListEntry.getKey());
                encounterOrdersMap.remove(encounterListEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(encounterListEntry.getKey(), new ArrayList<>(), drugOrdersList, patient, encounterListEntry.getKey().getEncounterProviders(), chiefComplaintList, medicalHistoryList, patientDocumentList, physicalExaminationList, procedure, orderList));
        }

        for(Map.Entry<Encounter, List<OpenMrsCondition>> encounterListEntry : encounterChiefComplaintsMap.entrySet()){
            List<OpenMrsCondition> medicalHistoryList = new ArrayList<>();
            List<Obs> physicalExaminationList = new ArrayList<>();
            List<Obs> patientDocumentList = new ArrayList<>();
            List<Order> orderList = new ArrayList<>();
            Obs procedure = null;
            List<OpenMrsCondition> chiefComplaintList = getEncounterConditions(encounterChiefComplaintsMap, encounterListEntry.getKey());
            if(encounterMedicalHistoryMap.get(encounterListEntry.getKey()) != null) {
                medicalHistoryList = getEncounterConditions(encounterMedicalHistoryMap, encounterListEntry.getKey());
                encounterMedicalHistoryMap.remove(encounterListEntry.getKey());
            }
            if(encounterPhysicalExaminationMap.get(encounterListEntry.getKey()) != null){
                physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, encounterListEntry.getKey());
                encounterPhysicalExaminationMap.remove(encounterListEntry.getKey());
            }
            if(encounterPatientDocumentsMap.get(encounterListEntry.getKey()) != null){
                patientDocumentList = getEncounterObs(encounterPatientDocumentsMap, encounterListEntry.getKey());
                encounterPatientDocumentsMap.remove(encounterListEntry.getKey());
            }
            if(encounterProcedureNotesMap.get(encounterListEntry.getKey()) != null) {
                procedure = getEncounterObsProcedure(encounterProcedureNotesMap, encounterListEntry.getKey());
                encounterProcedureNotesMap.remove(encounterListEntry.getKey());
            }
            if(encounterOrdersMap.get(encounterListEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, encounterListEntry.getKey());
                encounterOrdersMap.remove(encounterListEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(encounterListEntry.getKey(), new ArrayList<>(), new ArrayList<>(), patient, encounterListEntry.getKey().getEncounterProviders(), chiefComplaintList, medicalHistoryList, patientDocumentList, physicalExaminationList, procedure, orderList));
        }

        for(Map.Entry<Encounter, List<OpenMrsCondition>> medicalHistoryEntry : encounterMedicalHistoryMap.entrySet()){
            List<OpenMrsCondition> medicalHistoryList = getEncounterConditions(encounterChiefComplaintsMap, medicalHistoryEntry.getKey());
            List<Obs> physicalExaminationList = new ArrayList<>();
            List<Obs> patientDocumentList = new ArrayList<>();
            List<Order> orderList = new ArrayList<>();
            Obs procedure = null;
            if(encounterPhysicalExaminationMap.get(medicalHistoryEntry.getKey()) != null){
                physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, medicalHistoryEntry.getKey());
                encounterPhysicalExaminationMap.remove(medicalHistoryEntry.getKey());
            }
            if(encounterPatientDocumentsMap.get(medicalHistoryEntry.getKey()) != null){
                patientDocumentList = getEncounterObs(encounterPatientDocumentsMap, medicalHistoryEntry.getKey());
                encounterPatientDocumentsMap.remove(medicalHistoryEntry.getKey());
            }
            if(encounterProcedureNotesMap.get(medicalHistoryEntry.getKey()) != null) {
                procedure = getEncounterObsProcedure(encounterProcedureNotesMap, medicalHistoryEntry.getKey());
                encounterProcedureNotesMap.remove(medicalHistoryEntry.getKey());
            }
            if(encounterOrdersMap.get(medicalHistoryEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, medicalHistoryEntry.getKey());
                encounterOrdersMap.remove(medicalHistoryEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(medicalHistoryEntry.getKey(), new ArrayList<>(), new ArrayList<>(), patient, medicalHistoryEntry.getKey().getEncounterProviders(), new ArrayList<>(), medicalHistoryList, patientDocumentList, physicalExaminationList, procedure, orderList));
        }

        for(Map.Entry<Encounter, List<Obs>> physicalExaminationEntry : encounterPhysicalExaminationMap.entrySet()){
            List<Obs> physicalExaminationList = getEncounterObs(encounterPhysicalExaminationMap, physicalExaminationEntry.getKey());
            List<Obs> patientDocumentList = new ArrayList<>();
            List<Order> orderList = new ArrayList<>();
            Obs procedure = null;
            if(encounterPatientDocumentsMap.get(physicalExaminationEntry.getKey()) != null){
                patientDocumentList = getEncounterObs(encounterPatientDocumentsMap, physicalExaminationEntry.getKey());
                encounterPatientDocumentsMap.remove(physicalExaminationEntry.getKey());
            }
            if(encounterProcedureNotesMap.get(physicalExaminationEntry.getKey()) != null) {
                procedure = getEncounterObsProcedure(encounterProcedureNotesMap, physicalExaminationEntry.getKey());
                encounterProcedureNotesMap.remove(physicalExaminationEntry.getKey());
            }
            if(encounterOrdersMap.get(physicalExaminationEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, physicalExaminationEntry.getKey());
                encounterOrdersMap.remove(physicalExaminationEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(physicalExaminationEntry.getKey(), new ArrayList<>(), new ArrayList<>(), patient, physicalExaminationEntry.getKey().getEncounterProviders(), new ArrayList<>(), new ArrayList<>(), patientDocumentList, physicalExaminationList, procedure, orderList));
        }

        for(Map.Entry<Encounter, List<Obs>> patientDocumentEntry : encounterPatientDocumentsMap.entrySet()){
            List<Obs> patientDocumentList = getEncounterObs(encounterPatientDocumentsMap, patientDocumentEntry.getKey());
            List<Order> orderList = new ArrayList<>();
            Obs procedure = null;
            if(encounterProcedureNotesMap.get(patientDocumentEntry.getKey()) != null) {
                procedure = getEncounterObsProcedure(encounterProcedureNotesMap, patientDocumentEntry.getKey());
                encounterProcedureNotesMap.remove(patientDocumentEntry.getKey());
            }
            if(encounterOrdersMap.get(patientDocumentEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, patientDocumentEntry.getKey());
                encounterOrdersMap.remove(patientDocumentEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(patientDocumentEntry.getKey(), new ArrayList<>(), new ArrayList<>(), patient, patientDocumentEntry.getKey().getEncounterProviders(), new ArrayList<>(), new ArrayList<>(), patientDocumentList, new ArrayList<>(), procedure, orderList));
        }

        for(Map.Entry<Encounter, Obs> procedureMapEntry : encounterProcedureNotesMap.entrySet()){
            Obs procedure = getEncounterObsProcedure(encounterProcedureNotesMap, procedureMapEntry.getKey());
            List<Order> orderList = new ArrayList<>();
            if(encounterOrdersMap.get(procedureMapEntry.getKey()) != null) {
                orderList = getEncounterOrders(encounterOrdersMap, procedureMapEntry.getKey());
                encounterOrdersMap.remove(procedureMapEntry.getKey());
            }
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(procedureMapEntry.getKey(), new ArrayList<>(), new ArrayList<>(), patient, procedureMapEntry.getKey().getEncounterProviders(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), procedure, orderList));
        }

        for(Map.Entry<Encounter, List<Order>> orderMapEntry : encounterOrdersMap.entrySet()){
            openMrsDischargeSummaryList.add(new OpenMrsDischargeSummary(orderMapEntry.getKey(), new ArrayList<>(), new ArrayList<>(), patient, orderMapEntry.getKey().getEncounterProviders(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null, encounterOrdersMap.get(orderMapEntry.getKey())));
        }

        return openMrsDischargeSummaryList;
    }

    private static List<OpenMrsCondition> getEncounterConditions(Map<Encounter, List<OpenMrsCondition>> map, Encounter encounter) {
        if (map.containsKey(encounter)) {
            List<OpenMrsCondition> conditionList = map.get(encounter);
            map.remove(encounter);
            return conditionList;
        }
        return null;
    }

    private static List<Order> getEncounterOrders(Map<Encounter, List<Order>> map, Encounter encounter){
        if (map.containsKey(encounter)) {
            List<Order> orderList = map.get(encounter);
            map.remove(encounter);
            return orderList;
        }
        return null;
    }

    private static List<Obs> getEncounterObs(Map<Encounter, List<Obs>> map, Encounter encounter) {
        if (map.containsKey(encounter)) {
            List<Obs> obsList = map.get(encounter);
            map.remove(encounter);
            return obsList;
        }
        return null;
    }

    private static Obs getEncounterObsProcedure(Map<Encounter, Obs> map, Encounter encounter) {
        if (map.containsKey(encounter)) {
            Obs obs = map.get(encounter);
            map.remove(encounter);
            return obs;
        }
        return null;
    }
}
