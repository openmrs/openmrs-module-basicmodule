package org.bahmni.module.hip.web.service;


import org.junit.Test;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;

import static org.mockito.Mockito.*;


public class MedicationRequestServiceTest {

    private OpenMRSDrugOrderClient openMRSDrugOrderClient = mock(OpenMRSDrugOrderClient.class);
    private MedicationRequestTranslator medicationTranslator =
            mock(MedicationRequestTranslator.class);

    private MedicationRequestService medicationRequestService = new MedicationRequestService(
            openMRSDrugOrderClient,
            medicationTranslator
    );

    @Test
    public void shouldFetchAllDrugOrdersForVisitTypeOPDGivenAPatientID() {

        medicationRequestService.medicationRequestFor("0f90531a-285c-438b-b265-bb3abb4745bd", "OPD");

        verify(openMRSDrugOrderClient, times(1))
                .drugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "OPD");
    }
}
