package org.bahmni.module.hip.web.controller;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;


@Component
public class HipContext {
    private static final FhirContext fhirCtx = FhirContext.forR4();

    public static String encodeToString(Bundle bundle) {
        return fhirCtx.newJsonParser().encodeResourceToString(bundle);
    }
}
