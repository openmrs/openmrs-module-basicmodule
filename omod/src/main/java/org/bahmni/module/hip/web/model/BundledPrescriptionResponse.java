package org.bahmni.module.hip.web.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bahmni.module.hip.web.model.serializers.FhirBundleSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hl7.fhir.r4.model.Bundle;

@Builder
@Getter
@Setter
public class BundledPrescriptionResponse {
    private CareContext careContext;
    private Bundle bundle;

    public CareContext getCareContext() {
        return careContext;
    }
    @JsonSerialize(using = FhirBundleSerializer.class)
    public Bundle getBundle() {
        return bundle;
    }
}
