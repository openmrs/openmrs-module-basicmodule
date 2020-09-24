package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.FhirPrescription;
import org.bahmni.module.hip.web.model.OpenMrsPrescription;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionGenerator {
    private final CareContextService careContextService;

    @Autowired
    public PrescriptionGenerator(CareContextService careContextService) {
        this.careContextService = careContextService;
    }

    BundledPrescriptionResponse generate(OpenMrsPrescription openMrsPrescription) {

        Bundle prescriptionBundle = createPrescriptionBundle(openMrsPrescription);

        return BundledPrescriptionResponse.builder()
                .bundle(prescriptionBundle)
                .careContext(careContextService.careContextFor(openMrsPrescription.getEncounter(), getOrgContext().getCareContextType()))
                .build();
    }

    private Bundle createPrescriptionBundle(OpenMrsPrescription openMrsPrescription) {

        return FhirPrescription
                .from(openMrsPrescription)
                .bundle(webURL());
    }

    private OrgContext getOrgContext() {
        Organization organization = getOrganization();
        return OrgContext.builder()
                .organization(organization)
                .webUrl(webURL())
                .build();
    }

    private String webURL() {
        AdministrationService administrationService = Context.getAdministrationService();
        return administrationService.getGlobalProperty(Constants.PROP_HFR_URL);
    }

    private Organization getOrganization() {
        AdministrationService administrationService = Context.getAdministrationService();
        String hfrId = administrationService.getGlobalProperty(Constants.PROP_HFR_ID);
        String hfrName = administrationService.getGlobalProperty(Constants.PROP_HFR_NAME);
        String hfrSystem = administrationService.getGlobalProperty(Constants.PROP_HFR_SYSTEM);
        return FHIRUtils.createOrgInstance(hfrId, hfrName, hfrSystem);
    }

}
