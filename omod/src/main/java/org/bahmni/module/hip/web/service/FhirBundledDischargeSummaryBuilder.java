package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.*;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FhirBundledDischargeSummaryBuilder {
    private final CareContextService careContextService;
    private final OrganizationContextService organizationContextService;
    private final FHIRResourceMapper fhirResourceMapper;

    @Autowired
    public FhirBundledDischargeSummaryBuilder(CareContextService careContextService, OrganizationContextService organizationContextService, FHIRResourceMapper fhirResourceMapper) {
        this.careContextService = careContextService;
        this.organizationContextService = organizationContextService;
        this.fhirResourceMapper = fhirResourceMapper;
    }

    public DischargeSummaryBundle fhirBundleResponseFor (OpenMrsDischargeSummary openMrsDischargeSummary) {

        OrganizationContext organizationContext = organizationContextService.buildContext();

        Bundle dischargeSummaryBundle = FhirDischargeSummary.fromOpenMrsDischargeSummary(openMrsDischargeSummary, fhirResourceMapper).
                bundleDischargeSummary(organizationContext.webUrl());

        CareContext careContext = careContextService.careContextFor(
                openMrsDischargeSummary.getEncounter(),
                organizationContext.careContextType());

        return DischargeSummaryBundle.builder()
                .bundle(dischargeSummaryBundle)
                .careContext(careContext)
                .build();
    }
}
