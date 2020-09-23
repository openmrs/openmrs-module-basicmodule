package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;

import static org.mockito.Mockito.*;

public class OpenMRSDrugOrderClientTest {

    private PatientService patientService = mock(PatientService.class);
    private DrugOrderService drugOrderService = mock(DrugOrderService.class);
    private OrderService orderService = mock(OrderService.class);
    private PrescriptionOrderDao prescriptionOrderDao = mock(PrescriptionOrderDao.class);

    private OpenMRSDrugOrderClient openMRSDrugOrderClient = new OpenMRSDrugOrderClient(patientService, drugOrderService, orderService, prescriptionOrderDao);

    @Test
    public void shouldFetchThePatientFromTheOpenMRSGivenItsUUID() {

        openMRSDrugOrderClient.getDrugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "");

        verify(patientService, times(1))
                .getPatientByUuid("0f90531a-285c-438b-b265-bb3abb4745bd");
    }

    @Test
    public void shouldFetchAllDrugOrderOfAPatientGivenTheirPurposeOfVisit() {

        Patient patient = mock(Patient.class);

        when(patientService.getPatientByUuid(anyString())).thenReturn(patient);

        openMRSDrugOrderClient.getDrugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "OPD");

        verify(drugOrderService, times(1))
                .getAllDrugOrderFor(patient, "OPD");
    }
}
