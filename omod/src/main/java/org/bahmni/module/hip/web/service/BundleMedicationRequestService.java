package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BundleMedicationRequestService {

    private MedicationRequestService medicationRequestService;
    private BundleService bundleService;

    @Autowired
    public BundleMedicationRequestService(MedicationRequestService medicationRequestService, BundleService bundleService) {
        this.medicationRequestService = medicationRequestService;
        this.bundleService = bundleService;
    }

    public Bundle bundleMedicationRequestsFor(String patientId, String byVisitType) {

        List<MedicationRequest> medicationRequests = medicationRequestService.medicationRequestFor(patientId, byVisitType);

        return bundleService.bundleMedicationRequests(medicationRequests);
    }
}
