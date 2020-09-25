package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.*;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FhirBundledPrescriptionBuilder {
    private final CareContextService careContextService;
    private final OrganizationContextService organizationContextService;

    @Autowired
    public FhirBundledPrescriptionBuilder(CareContextService careContextService, OrganizationContextService organizationContextService) {
        this.careContextService = careContextService;
        this.organizationContextService = organizationContextService;
    }

    BundledPrescriptionResponse fhirBundleResponseFor(OpenMrsPrescription openMrsPrescription) {

        OrganizationContext organizationContext = organizationContextService.buildContext();

        Bundle prescriptionBundle = FhirPrescription
                .from(openMrsPrescription)
                .bundle(organizationContext.webUrl());

        CareContext careContext = careContextService.careContextFor(
                openMrsPrescription.getEncounter(),
                organizationContext.careContextType());

        return BundledPrescriptionResponse.builder()
                .bundle(prescriptionBundle)
                .careContext(careContext)
                .build();
    }
}
