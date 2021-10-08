package org.bahmni.module.hip.web.service;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.VisitType;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationServiceTest {
    private final VisitService visitService = mock(VisitService.class);
    private final PatientService patientService = mock(PatientService.class);
    private final ProgramWorkflowService programWorkflowService = mock(ProgramWorkflowService.class);
    private final ExistingPatientService existingPatientService = mock(ExistingPatientService.class);
    private final ValidationService validationService = new ValidationService(visitService,patientService, programWorkflowService, existingPatientService);
    @Test
    public void shouldReturnTrueForValidVisitType() {
        String visitType = "OPD";
        when(visitService.getAllVisitTypes()).thenReturn(Collections.singletonList(new VisitType("OPD", "OPD")));
        boolean actual = validationService.isValidVisit(visitType);

        assertTrue(actual);
    }

    @Test
    public void shouldReturnTrueForValidPatientId() {
        Patient patient = mock(Patient.class);
        String patientId = "0f90531a-285c-438b-b265-bb3abb4745bd";
        patient.setUuid(patientId);
        when(patientService.getPatientByUuid(patientId)).thenReturn(patient);
        boolean actual = validationService.isValidPatient(patientId);

        assertTrue(actual);
    }

}