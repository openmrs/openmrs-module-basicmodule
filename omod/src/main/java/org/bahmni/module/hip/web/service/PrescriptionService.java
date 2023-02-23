package org.bahmni.module.hip.web.service;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.hip.api.dao.HipVisitDao;
import org.bahmni.module.hip.web.model.PrescriptionBundle;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.DrugOrders;
import org.bahmni.module.hip.web.model.OpenMrsPrescription;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private static Logger logger = LogManager.getLogger(PrescriptionService.class);

    private final OpenMRSDrugOrderClient openMRSDrugOrderClient;
    private final FhirBundledPrescriptionBuilder fhirBundledPrescriptionBuilder;
    private final PatientService patientService;
    private final HipVisitDao hipVisitDao;

    @Autowired
    public PrescriptionService(OpenMRSDrugOrderClient openMRSDrugOrderClient, FhirBundledPrescriptionBuilder fhirBundledPrescriptionBuilder, PatientService patientService, HipVisitDao hipVisitDao) {
        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
        this.fhirBundledPrescriptionBuilder = fhirBundledPrescriptionBuilder;
        this.patientService = patientService;
        this.hipVisitDao = hipVisitDao;
    }


    public List<PrescriptionBundle> getPrescriptions(String patientUuid,String visitType, Date visitStartDate) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Visit visit = hipVisitDao.getPatientVisit(patient,visitType,visitStartDate);
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndVisitTypeFor(visit));

        if (drugOrders.isEmpty())
            return new ArrayList<>();

        List<OpenMrsPrescription> openMrsPrescriptions = OpenMrsPrescription
                .from(drugOrders.groupByEncounter());

        return openMrsPrescriptions
                .stream()
                .map(fhirBundledPrescriptionBuilder::fhirBundleResponseFor)
                .collect(Collectors.toList());
    }

    public List<PrescriptionBundle> getPrescriptionsForProgram(String patientIdUuid, DateRange dateRange, String programName, String programEnrolmentId) {
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateAndProgramFor(patientIdUuid, dateRange, programName, programEnrolmentId));

        if (drugOrders.isEmpty())
            return new ArrayList<>();

        List<OpenMrsPrescription> openMrsPrescriptions = OpenMrsPrescription
                .from(drugOrders.groupByEncounter());

        return openMrsPrescriptions
                .stream()
                .map(fhirBundledPrescriptionBuilder::fhirBundleResponseFor)
                .collect(Collectors.toList());
    }
}
