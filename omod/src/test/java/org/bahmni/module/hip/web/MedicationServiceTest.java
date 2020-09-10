package org.bahmni.module.hip.web;


import org.bahmni.module.hip.web.service.MedicationService;
import org.junit.Test;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;


public class MedicationServiceTest {

    private PatientService patientService = mock(PatientService.class);
    private OrderService orderService = mock(OrderService.class);

    private MedicationService medicationService = new MedicationService(patientService, orderService);

    @Test
    public void testShouldReturnPatientIdIsRequiredErrorMessage() {

        String medicationResponse = medicationService.getMedication("", "OPD");

        assertEquals("Patient id and visit type are required.", medicationResponse);

    }
}
