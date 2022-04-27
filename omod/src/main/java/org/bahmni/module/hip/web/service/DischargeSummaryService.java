package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.DischargeSummaryDao;
import org.bahmni.module.hip.web.model.OpenMrsCondition;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.DischargeSummaryBundle;
import org.bahmni.module.hip.web.model.DrugOrders;
import org.bahmni.module.hip.web.model.OpenMrsDischargeSummary;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DischargeSummaryService {

    private final PatientService patientService;
    private final DischargeSummaryDao dischargeSummaryDao;
    private final FhirBundledDischargeSummaryBuilder fhirBundledDischargeSummaryBuilder;
    private final OpenMRSDrugOrderClient openMRSDrugOrderClient;
    private final ConsultationService consultationService;

    @Autowired
    public DischargeSummaryService(PatientService patientService, DischargeSummaryDao dischargeSummaryDao, FhirBundledDischargeSummaryBuilder fhirBundledDischargeSummaryBuilder, OpenMRSDrugOrderClient openMRSDrugOrderClient, ConsultationService consultationService) {
        this.patientService = patientService;
        this.dischargeSummaryDao = dischargeSummaryDao;
        this.fhirBundledDischargeSummaryBuilder = fhirBundledDischargeSummaryBuilder;
        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
        this.consultationService = consultationService;
    }

    public List<DischargeSummaryBundle> getDischargeSummaryForVisit(String patientUuid, DateRange dateRange, String visitType, Date visitStartDate) {
        Date fromDate = dateRange.getFrom();
        Date toDate = dateRange.getTo();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndVisitTypeFor(patientUuid, dateRange, visitType,visitStartDate));
        Map<Encounter, DrugOrders> encounteredDrugOrdersMap = drugOrders.groupByEncounter();
        Map<Encounter, List<Obs>> encounterDischargeSummaryMap = getEncounterCarePlanMap(patient, visitType, visitStartDate, fromDate, toDate);
        ConcurrentHashMap<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = consultationService.getEncounterChiefComplaintsMap(patient, visitType, visitStartDate, fromDate, toDate);
        Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap = consultationService.getEncounterMedicalHistoryConditionsMap(patient, visitType, visitStartDate, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPhysicalExaminationMap = consultationService.getEncounterPhysicalExaminationMap(patient, visitType, visitStartDate, fromDate, toDate);
        Map<Encounter, List<Obs>> encounterPatientDocumentsMap = consultationService.getEncounterPatientDocumentsMap(visitType, visitStartDate, fromDate, toDate, patient);
        Map<Encounter, Obs> encounterProcedureMap = getEncounterProcedureMap(patient, visitType, visitStartDate, fromDate, toDate);
        Map<Encounter, List<Order>> encounterOrdersMap = consultationService.getEncounterOrdersMap(visitType, visitStartDate, fromDate, toDate, patient);

        List<OpenMrsDischargeSummary> openMrsDischargeSummaryList = OpenMrsDischargeSummary.getOpenMrsDischargeSummaryList(encounterDischargeSummaryMap, encounteredDrugOrdersMap, encounterChiefComplaintsMap, encounterMedicalHistoryMap, encounterPhysicalExaminationMap, encounterPatientDocumentsMap, encounterProcedureMap, encounterOrdersMap, patient);
        return openMrsDischargeSummaryList.stream().map(fhirBundledDischargeSummaryBuilder::fhirBundleResponseFor).collect(Collectors.toList());
    }

    public List<DischargeSummaryBundle> getDischargeSummaryForProgram(String patientUuid, DateRange dateRange, String programName,String programEnrollmentId){
        Date fromDate = dateRange.getFrom();
        Date toDate = dateRange.getTo();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndProgramFor(patientUuid, dateRange, programName,programEnrollmentId));
        Map<Encounter, DrugOrders> encounteredDrugOrdersMap = drugOrders.groupByEncounter();
        ConcurrentHashMap<Encounter, List<OpenMrsCondition>> encounterChiefComplaintsMap = consultationService.getEncounterChiefComplaintsMapForProgram(programName,fromDate, toDate,patient);
        Map<Encounter, List<Obs>> encounterDischargeSummaryMap = getEncounterCarePlanMapForProgram(programName,fromDate,toDate,patient);
        Map<Encounter, List<OpenMrsCondition>> encounterMedicalHistoryMap = consultationService.getEncounterMedicalHistoryConditionsMapForProgram(programName,fromDate, toDate,patient);
        Map<Encounter, List<Obs>> encounterPhysicalExaminationMap = consultationService.getEncounterPhysicalExaminationMapForProgram(programName,fromDate, toDate,patient);
        Map<Encounter, List<Obs>> encounterPatientDocumentsMap = consultationService.getEncounterPatientDocumentsMapForProgram(programName,fromDate,toDate,patient,programEnrollmentId);
        Map<Encounter, Obs> encounterProcedureMap = getEncounterProcedureMapForProgram(programName,fromDate,toDate,patient);
        Map<Encounter, List<Order>> encounterOrdersMap = consultationService.getEncounterOrdersMapForProgram(programName,fromDate,toDate,patient);
        List<OpenMrsDischargeSummary> openMrsDischargeSummaryList = OpenMrsDischargeSummary.getOpenMrsDischargeSummaryList(encounterDischargeSummaryMap, encounteredDrugOrdersMap, encounterChiefComplaintsMap, encounterMedicalHistoryMap, encounterPhysicalExaminationMap, encounterPatientDocumentsMap, encounterProcedureMap, encounterOrdersMap, patient);
        return openMrsDischargeSummaryList.stream().map(fhirBundledDischargeSummaryBuilder::fhirBundleResponseFor).collect(Collectors.toList());
    }


    private Map<Encounter, List<Obs>> getEncounterCarePlanMap(Patient patient, String visitType,  Date visitStartDate, Date fromDate, Date toDate) {
        List<Obs> carePlanObs = dischargeSummaryDao.getCarePlan(patient, visitType,visitStartDate, fromDate, toDate);
        return getEncounterListMapForCarePlan(carePlanObs);
    }

    private Map<Encounter, List<Obs>> getEncounterListMapForCarePlan(List<Obs> carePlanObs) {
        Map<Encounter, List<Obs>> encounterCarePlanMap = new HashMap<>();
        for(Obs obs : carePlanObs){
            Encounter encounter = obs.getEncounter();
            if(!encounterCarePlanMap.containsKey(encounter)){
                encounterCarePlanMap.put(encounter, new ArrayList<>());
            }
            encounterCarePlanMap.get(encounter).add(obs);
        }
        return encounterCarePlanMap;
    }

    private Map<Encounter, List<Obs>> getEncounterCarePlanMapForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<Obs> carePlanObs = dischargeSummaryDao.getCarePlanForProgram(programName,fromDate, toDate,patient);
        return getEncounterListMapForCarePlan(carePlanObs);
    }

    private Map<Encounter, Obs> getEncounterProcedureMap(Patient patient, String visitType, Date visitStartDate, Date fromDate, Date toDate) {
        List<Obs> obsProcedures = dischargeSummaryDao.getProcedures(patient, visitType, visitStartDate, fromDate, toDate);
        Map<Encounter, Obs> encounterProcedureMap = new HashMap<>();
        for(Obs o: obsProcedures){
            encounterProcedureMap.put(o.getEncounter(), o);
        }
        return encounterProcedureMap;
    }

    private Map<Encounter, Obs> getEncounterProcedureMapForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<Obs> obsProcedures = dischargeSummaryDao.getProceduresForProgram(programName,fromDate, toDate,patient);
        Map<Encounter, Obs> encounterProcedureMap = new HashMap<>();
        for(Obs o: obsProcedures){
            encounterProcedureMap.put(o.getEncounter(), o);
        }
        return encounterProcedureMap;
    }


}
