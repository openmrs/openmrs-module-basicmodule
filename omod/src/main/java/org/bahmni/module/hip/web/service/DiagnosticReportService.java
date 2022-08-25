package org.bahmni.module.hip.web.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.hip.api.dao.DiagnosticReportDao;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.bahmni.module.hip.api.dao.HipVisitDao;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.DiagnosticReportBundle;
import org.bahmni.module.hip.web.model.OpenMrsDiagnosticReport;
import org.bahmni.module.hip.web.model.OpenMrsLabResults;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
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
import java.util.Objects;
import java.util.Optional;
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
    private final String ORDER_TYPE = "Order";
    private static final String RADIOLOGY_TYPE = "RADIOLOGY";
    private static final String PATIENT_DOCUMENT_TYPE = "Patient Document";
    private static final String DOCUMENT_TYPE = "Document";
    private static Logger logger = LogManager.getLogger(DiagnosticReportService.class);


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
        List<Obs> patientObs = encounterDao.GetAllObsForVisit(visit, RADIOLOGY_TYPE, DOCUMENT_TYPE);
        patientObs.addAll(encounterDao.GetAllObsForVisit(visit, PATIENT_DOCUMENT_TYPE, DOCUMENT_TYPE));
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

    private  Map<Obs , String> getLabUploadsMap(Map<Encounter,List<Obs>> orderedTestUploads, Map<Encounter,List<Obs>> unorderedUploads, Order order) {
        Encounter encounter = order.getEncounter();
        Map<Obs , String> labReports = new HashMap<>();
        if(orderedTestUploads.containsKey(encounter)) putAllObsIntoMap(orderedTestUploads.get(encounter),labReports);
        if(unorderedUploads.containsKey(encounter)) {
            putAllObsIntoMap(unorderedUploads.get(encounter),labReports);
            unorderedUploads.remove(encounter);
        }
        logger.warn("UPLOAD MAP " + labReports);
        return labReports;
    }

    private void putAllObsIntoMap(List<Obs> obs,Map<Obs , String> labReports) {
        for (Obs o: obs) {
            labReports.put(o, diagnosticReportDao.getTestNameForLabReports(o));
        }
    }

    private List<DiagnosticReportBundle> getLabResults(Patient patient, List<Visit> visitList) {

        Map<Encounter,List<Obs>> orderedTestUploads = diagnosticReportDao.getAllOrderedTestUploads(patient.getUuid(), visitList.size() != 0 ? visitList.get(0) : null);
        Map<Encounter,List<Obs>> unorderedUploads = diagnosticReportDao.getAllUnorderedUploadsForVisit(patient.getUuid(), visitList.size() != 0 ? visitList.get(0) : null);

        LabOrderResults results = labOrderResultsService.getAll(patient, visitList, Integer.MAX_VALUE);
        Map<String, List<LabOrderResult>> groupedByOrderUUID = results.getResults().stream().collect(Collectors.groupingBy(LabOrderResult::getOrderUuid));

        List<Order> orders = orderDao.getAllOrdersForVisits(new OrderType(4), visitList);

        List<OpenMrsLabResults> labResults = groupedByOrderUUID.entrySet().stream().map(entry -> {
            Optional<Order> orderForUuid = orders
                    .stream()
                    .filter(order -> order.getUuid().equals(entry.getKey()))
                    .findFirst();
            if (orderForUuid.isPresent()) {
                return new OpenMrsLabResults(orderForUuid.get().getEncounter(), orderForUuid.get().getPatient(), entry.getValue(),
                        getLabUploadsMap(orderedTestUploads,unorderedUploads,orderForUuid.get()));
            }
            else return null;
        } ).filter(Objects::nonNull).collect(Collectors.toList());


        labResults.addAll(unorderedUploads.entrySet().stream().map(entry -> {
            Map<Obs , String> labReports = new HashMap<>();
            putAllObsIntoMap(entry.getValue(),labReports);
            return new OpenMrsLabResults(entry.getKey(), entry.getKey().getPatient(), new ArrayList<>(),labReports);
        } ).collect(Collectors.toList()));

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

        visits = orderDao.getVisitsWithAllOrders(patient, ORDER_TYPE, null, null );
        visitsWithOrdersForProgram = visits.stream().filter( visit -> visitsForProgram.contains(visit.getVisitId()) ).collect(Collectors.toList());;
        return getLabResults(patient, visitsWithOrdersForProgram);
    }

}
