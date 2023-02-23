package org.bahmni.module.hip.web.model.serializers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.r4.model.Bundle;

public class BundleSerializer {
    public static String serializeBundle(Bundle bundle) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        return parser.encodeResourceToString(bundle);
    }
}
