package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.OrganizationContext;
import org.hl7.fhir.r4.model.Organization;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Service;

@Service
class OrganizationContextService {

    OrganizationContext buildContext() {
        Organization organization = createOrganizationInstance();
        return OrganizationContext.builder()
                .organization(organization)
                .webUrl(webURL())
                .build();
    }

    private String webURL() {
        AdministrationService administrationService = Context.getAdministrationService();
        return administrationService.getGlobalProperty(Constants.PROP_HFR_URL);
    }

    private Organization createOrganizationInstance() {
        AdministrationService administrationService = Context.getAdministrationService();
        String hfrId = administrationService.getGlobalProperty(Constants.PROP_HFR_ID);
        String hfrName = administrationService.getGlobalProperty(Constants.PROP_HFR_NAME);
        String hfrSystem = administrationService.getGlobalProperty(Constants.PROP_HFR_SYSTEM);
        return FHIRUtils.createOrgInstance(hfrId, hfrName, hfrSystem);
    }
}
