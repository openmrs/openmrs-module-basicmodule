package org.bahmni.module.hip.web.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BundleService {

    Bundle bundleMedicationRequests(List<MedicationRequest> medicationRequests) {
        Bundle bundle = new Bundle();

        medicationRequests
                .forEach(medicationRequest -> {
                    bundle.addEntry().setResource(medicationRequest);
                });

        return bundle;
    }

    public String serializeBundle(Bundle bundle){
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        return parser.encodeResourceToString(bundle);
    }
}
