package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationRequestService {

    private OpenMRSDrugOrderClient openMRSDrugOrderClient;

    @Autowired
    public MedicationRequestService(OpenMRSDrugOrderClient openMRSDrugOrderClient, BundleService bundleService) {
        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
    }

    List<MedicationRequest> medicationRequestFor(String patientId, String byVisitType){

        List<DrugOrder> drugOrders = openMRSDrugOrderClient.getDrugOrdersFor(patientId, byVisitType);

        return drugOrders
                .stream()
                .map(DrugOrderToMedicationRequestTranslationService::toMedicationRequest)
                .collect(Collectors.toList());
    }
}
