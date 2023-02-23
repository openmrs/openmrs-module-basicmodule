package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.Config;
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
        return administrationService.getGlobalProperty(Config.PROP_HFR_URL.getValue());
    }

    private Organization createOrganizationInstance() {
        AdministrationService administrationService = Context.getAdministrationService();
        String hfrId = administrationService.getGlobalProperty(Config.PROP_HFR_ID.getValue());
        String hfrName = administrationService.getGlobalProperty(Config.PROP_HFR_NAME.getValue());
        String hfrSystem = administrationService.getGlobalProperty(Config.PROP_HFR_SYSTEM.getValue());
        return FHIRUtils.createOrgInstance(hfrId, hfrName, hfrSystem);
    }
}
