package org.bahmni.module.hip.web.service;


import org.bahmni.module.hip.web.exception.NoMedicationFoundException;
import org.bahmni.module.hip.web.service.MedicationService;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;

import java.util.ArrayList;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MedicationServiceTest {

    private PatientService patientService = mock(PatientService.class);
    private OrderService orderService = mock(OrderService.class);

    private MedicationService medicationService = new MedicationService(patientService, orderService);

    @Test
    public void testShouldReturnPatientIdIsRequiredErrorMessage() {

        String medicationResponse = medicationService.getMedication("", "OPD");

        assertEquals("Patient id and visit type are required.", medicationResponse);
    }

    @Test
    public void testShouldThrowNoMedicationFoundExceptionGivenPatientHasNoOrders() {

        Patient patient = new Patient(123);

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);
        when(orderService.getAllOrdersByPatient(any())).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(NoMedicationFoundException.class, () -> {
            medicationService.getMedication("3f747cd9-255f-437d-b907-6e84b97a689d", "OPD");
        });

        String expectedMessage = "No Medication found for Patient";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }
}
