package org.bahmni.module.hip.web;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.bahmni.module.hip.web.service.CareContextService;
import org.bahmni.module.hip.web.service.PrescriptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;
@Configuration
@EnableWebMvc
public class TestConfiguration {
    @Bean
    public BundleMedicationRequestService bundleMedicationRequestService() {
        return mock(BundleMedicationRequestService.class);
    }

    @Bean
    public PrescriptionService prescriptionService() {
        return mock(PrescriptionService.class);
    }

    @Bean
    public CareContextService careContextService() {
        return mock(CareContextService.class);
    }
}