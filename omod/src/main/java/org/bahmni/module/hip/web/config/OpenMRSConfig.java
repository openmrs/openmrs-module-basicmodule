package org.bahmni.module.hip.web.config;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenMRSConfig {

    @Bean
    public PatientService patientService() {
        return Context.getPatientService();
    }

    @Bean
    public OrderService orderService() {
        return Context.getOrderService();
    }

    @Bean
    public AdministrationService administrationService() {
        return Context.getAdministrationService();
    }
}
