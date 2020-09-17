
package org.bahmni.module.hip.web.service;


import org.junit.Test;

import static org.mockito.Mockito.*;


public class MedicationRequestServiceTest {

    private OpenMRSDrugOrderClient openMRSDrugOrderClient = mock(OpenMRSDrugOrderClient.class);
    private DrugOrderToMedicationRequestTranslationService drugOrderToMedicationRequestTranslationService =
            mock(DrugOrderToMedicationRequestTranslationService.class);

    private MedicationRequestService medicationRequestService = new MedicationRequestService(
            openMRSDrugOrderClient,
            drugOrderToMedicationRequestTranslationService
    );

    @Test
    public void shouldFetchAllDrugOrdersForVisitTypeOPDGivenAPatientID() {

        medicationRequestService.medicationRequestFor("0f90531a-285c-438b-b265-bb3abb4745bd", "OPD");

        verify(openMRSDrugOrderClient, times(1))
                .getDrugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "OPD");
    }
}
