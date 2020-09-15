package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FhirBundleService {

    public Bundle bundleMedicationRequests(List<MedicationRequest> medicationRequests) {
        Bundle bundle = new Bundle();

        medicationRequests
                .forEach(medicationRequest -> {
                    bundle.addEntry().setResource(medicationRequest);
                });

        return bundle;
    }
}
