package org.bahmni.module.hip.web.service;

import org.openmrs.api.AdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BahmniAdministrationService {
    private final AdministrationService administrationService;

    @Autowired
    public BahmniAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    public String getWebUrl() {
        return administrationService.getGlobalProperty(Constants.PROP_HFR_URL);
    }
}
