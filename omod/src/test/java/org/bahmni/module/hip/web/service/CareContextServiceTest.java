package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.CareContextRepository;
import org.bahmni.module.hip.model.PatientCareContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CareContextServiceTest {
    private CareContextRepository careContextRepository = mock(CareContextRepository.class);

    private CareContextService careContextServiceObject = new CareContextService(careContextRepository);

    @Test
    public void shouldFetchAllCareContextForPatient() {
        String patientId = "0f90531a-285c-438b-b265-bb3abb4745bd";
        List<PatientCareContext> careContexts = new ArrayList<>();

        when(careContextRepository.getPatientCareContext(patientId))
                .thenReturn(careContexts);

        careContextServiceObject.careContextForPatient(patientId);

        verify(careContextRepository, times(1))
                .getPatientCareContext(patientId);
    }
}
