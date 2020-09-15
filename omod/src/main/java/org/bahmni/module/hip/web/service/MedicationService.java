package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.exception.NoMedicationFoundException;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    private DrugOrderService drugOrderService;
    private FhirBundleService fhirBundleService;

    @Autowired
    public MedicationService(DrugOrderService drugOrderService, FhirBundleService fhirBundleService) {
        this.drugOrderService = drugOrderService;
        this.fhirBundleService = fhirBundleService;
    }

    public Bundle bundleMedicationRequestsFor(String patientId, String byVisitType) {

        List<DrugOrder> drugOrders = drugOrderService.getDrugOrdersFor(patientId, byVisitType);

        if (drugOrders.isEmpty())
            throw new NoMedicationFoundException(patientId);

        List<MedicationRequest> medicationRequests = drugOrders
                .stream()
                .map(DrugOrderToMedicationRequestTranslationService::toMedicationRequest)
                .collect(Collectors.toList());

        return fhirBundleService.bundleMedicationRequests(medicationRequests);
    }
}
