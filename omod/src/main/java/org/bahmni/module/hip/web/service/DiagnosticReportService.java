package org.bahmni.module.hip.web.service;

import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.api.dao.DiagnosticReportDao;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.bahmni.module.hip.api.dao.HipVisitDao;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.DiagnosticReportBundle;
import org.bahmni.module.hip.web.model.OpenMrsDiagnosticReport;
import org.bahmni.module.hip.web.model.OpenMrsLabResults;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.openmrs.module.bahmniemrapi.laborder.service.LabOrderResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiagnosticReportService {
    private final FhirBundledDiagnosticReportBuilder fhirBundledDiagnosticReportBuilder;
    private final PatientService patientService;
    private final EncounterService encounterService;
    private final EncounterDao encounterDao;
    private HipVisitDao hipVisitDao;
    private OrderDao orderDao;
    private final DiagnosticReportDao diagnosticReportDao;


    private LabOrderResultsService labOrderResultsService;


    @Autowired
    public DiagnosticReportService(FhirBundledDiagnosticReportBuilder fhirBundledDiagnosticReportBuilder,
                                   PatientService patientService,
                                   EncounterService encounterService,
                                   LabOrderResultsService labOrderResultsService,
                                   EncounterDao encounterDao,
                                   HipVisitDao hipVisitDao,
                                   OrderDao orderDao,
                                   DiagnosticReportDao diagnosticReportDao) {
        this.fhirBundledDiagnosticReportBuilder = fhirBundledDiagnosticReportBuilder;
        this.patientService = patientService;
        this.encounterService = encounterService;
        this.encounterDao = encounterDao;
        this.hipVisitDao = hipVisitDao;
        this.labOrderResultsService = labOrderResultsService;
        this.orderDao = orderDao;
        this.diagnosticReportDao = diagnosticReportDao;
    }

    public List<DiagnosticReportBundle> getDiagnosticReportsForVisit(String patientUuid, String visitType, Date visitStartDate) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Visit visit = hipVisitDao.getPatientVisit(patient,visitType,visitStartDate);
        HashMap<Encounter, List<Obs>> encounterListMap = getAllObservationsForVisits(visit);
        List<OpenMrsDiagnosticReport> openMrsDiagnosticReports = OpenMrsDiagnosticReport.fromDiagnosticReport(encounterListMap);

        return openMrsDiagnosticReports
                .stream()
                .map(fhirBundledDiagnosticReportBuilder::fhirBundleResponseFor)
                .collect(Collectors.toList());
    }

    public HashMap<Encounter, List<Obs>> getAllObservationsForVisits(Visit visit) {
        List<Obs> patientObs = encounterDao.GetAllObsForVisit(visit, Config.RADIOLOGY_TYPE.getValue(), Config.DOCUMENT_TYPE.getValue());
        patientObs.addAll(encounterDao.GetAllObsForVisit(visit, Config.PATIENT_DOCUMENT.getValue(), Config.DOCUMENT_TYPE.getValue()));
        HashMap<Encounter, List<Obs>> encounterListMap = new HashMap<>();
        for (Obs obs: patientObs) {
            Encounter encounter = obs.getEncounter();
            if(!encounterListMap.containsKey(encounter))
               encounterListMap.put(encounter, new ArrayList<Obs>(){{ add(obs); }});
            encounterListMap.get(encounter).add(obs);
        }
        return encounterListMap;

    }

    public List<DiagnosticReportBundle> getDiagnosticReportsForProgram(String patientUuid, DateRange dateRange, String programName, String programEnrollmentId) {
        Date fromDate = dateRange.getFrom();
        Date toDate = dateRange.getTo();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        HashMap<Encounter, List<Obs>> encounterListMap = getAllObservationsForPrograms(fromDate, toDate, patient, programName, programEnrollmentId);
        List<OpenMrsDiagnosticReport> openMrsDiagnosticReports = OpenMrsDiagnosticReport.fromDiagnosticReport(encounterListMap);

        return openMrsDiagnosticReports
                .stream()
                .map(fhirBundledDiagnosticReportBuilder::fhirBundleResponseFor)
                .collect(Collectors.toList());

    }

    public HashMap<Encounter, List<Obs>> getAllObservationsForPrograms(Date fromDate, Date toDate,
                                                                        Patient patient,
                                                                        String programName,
                                                                        String programEnrollmentId) {
        HashMap<Encounter, List<Obs>> encounterListMap = new HashMap<>();
        List<Integer> encounterIds = encounterDao.GetEncounterIdsForProgramForDiagnosticReport(patient.getUuid(), programName,
                programEnrollmentId, fromDate, toDate);
        List<Encounter> finalList = new ArrayList<>();
        for(Integer encounterId : encounterIds){
            finalList.add(encounterService.getEncounter(encounterId));
        }
        for (Encounter e : finalList) {
            encounterListMap.put(e, new ArrayList<>(e.getAllObs()));
        }
        return encounterListMap;
    }


    private void putAllUnOrderedObsUploadsIntoMap(List<Obs> observations, Map<Obs, List<LabOrderResult>> labRecordsMap) {
        for (Obs obs: observations) {
            labRecordsMap.put(obs,new ArrayList<>());
        }
    }

    private List<DiagnosticReportBundle> getLabResults(Patient patient, List<Visit> visitList) {

        Map<Encounter,List<Obs>> orderedTestUploads = diagnosticReportDao.getAllOrderedTestUploads(patient.getUuid(), visitList.size() != 0 ? visitList.get(0) : null);
        Map<Encounter,List<Obs>> unorderedUploads = diagnosticReportDao.getAllUnorderedUploadsForVisit(patient.getUuid(), visitList.size() != 0 ? visitList.get(0) : null);

        LabOrderResults results = labOrderResultsService.getAll(patient, visitList, Integer.MAX_VALUE);
        Map<String, List<LabOrderResult>> groupedByOrderUUID = results.getResults().stream().collect(Collectors.groupingBy(LabOrderResult::getOrderUuid));

        List<OpenMrsLabResults> labResults = new ArrayList<>();
        Map<Obs, List<LabOrderResult>> labRecordsMap;

        for (Map.Entry<Encounter, List<Obs>> map : orderedTestUploads.entrySet()) {
            labRecordsMap = new HashMap<>();
            for (Obs obs: map.getValue()) {
                labRecordsMap.put(obs,groupedByOrderUUID.get(obs.getOrder().getUuid()));
            }
            if(unorderedUploads.containsKey(map.getKey()))
            {
                putAllUnOrderedObsUploadsIntoMap(unorderedUploads.get(map.getKey()),labRecordsMap);
                unorderedUploads.remove(map.getKey());
            }
           labResults.add(new OpenMrsLabResults(map.getKey(),map.getKey().getPatient(),labRecordsMap));
        }

        for (Map.Entry<Encounter, List<Obs>> map : unorderedUploads.entrySet()) {
            labRecordsMap = new HashMap<>();
            putAllUnOrderedObsUploadsIntoMap(unorderedUploads.get(map.getKey()),labRecordsMap);
            labResults.add(new OpenMrsLabResults(map.getKey(),map.getKey().getPatient(),labRecordsMap));
        }

        List<DiagnosticReportBundle> bundles = labResults.stream().map(fhirBundledDiagnosticReportBuilder::fhirBundleResponseFor).collect(Collectors.toList());
        return bundles;
    }

    public List<DiagnosticReportBundle> getLabResultsForVisits(String patientUuid, String visittype, Date visitStartDate)
    {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Visit visit = hipVisitDao.getPatientVisit(patient,visittype,visitStartDate);
        List<Visit> visits = new ArrayList<>();

        visits.add(visit);
        return getLabResults(patient, visits);
    }

    public List<DiagnosticReportBundle> getLabResultsForPrograms(String patientUuid, DateRange dateRange, String programName, String programEnrollmentId)
    {
        List<Integer> visitsForProgram =  hipVisitDao.GetVisitIdsForProgramForLabResults(patientUuid, programName, programEnrollmentId, dateRange.getFrom(), dateRange.getTo() );
        Patient patient = patientService.getPatientByUuid(patientUuid);

        List<Visit> visits, visitsWithOrdersForProgram ;

        visits = orderDao.getVisitsWithAllOrders(patient, Config.ORDER_TYPE.getValue(), null, null );
        visitsWithOrdersForProgram = visits.stream().filter( visit -> visitsForProgram.contains(visit.getVisitId()) ).collect(Collectors.toList());;
        return getLabResults(patient, visitsWithOrdersForProgram);
    }

}
