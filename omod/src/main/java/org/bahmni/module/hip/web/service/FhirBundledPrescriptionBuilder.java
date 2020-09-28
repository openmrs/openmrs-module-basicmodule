package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.*;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FhirBundledPrescriptionBuilder {
    private final CareContextService careContextService;
    private final OrganizationContextService organizationContextService;
    private final FHIRResourceMapper fhirResourceMapper;

    @Autowired
    public FhirBundledPrescriptionBuilder(CareContextService careContextService, OrganizationContextService organizationContextService, FHIRResourceMapper fhirResourceMapper) {
        this.careContextService = careContextService;
        this.organizationContextService = organizationContextService;
        this.fhirResourceMapper = fhirResourceMapper;
    }

    BundledPrescriptionResponse fhirBundleResponseFor(OpenMrsPrescription openMrsPrescription) {

        OrganizationContext organizationContext = organizationContextService.buildContext();

        Bundle prescriptionBundle = FhirPrescription
                .from(openMrsPrescription, fhirResourceMapper)
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
