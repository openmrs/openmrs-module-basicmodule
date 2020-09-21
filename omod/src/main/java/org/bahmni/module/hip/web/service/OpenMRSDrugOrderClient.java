package org.bahmni.module.hip.web.service;

import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenMRSDrugOrderClient {

    private PatientService patientService;
    private DrugOrderService drugOrderService;

    @Autowired
    public OpenMRSDrugOrderClient(PatientService patientService, DrugOrderService drugOrderService) {
        this.patientService = patientService;
        this.drugOrderService = drugOrderService;
    }

    List<DrugOrder> getDrugOrdersFor(String forPatientUUID, String byTheirVisitType) {

        Patient patient = patientService.getPatientByUuid(forPatientUUID);

        return drugOrderService.getAllDrugOrderFor(patient, byTheirVisitType);
    }
}
