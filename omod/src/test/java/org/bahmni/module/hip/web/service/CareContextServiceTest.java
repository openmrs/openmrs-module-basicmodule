package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.CareContextRepository;
import org.bahmni.module.hip.api.dao.ExistingPatientDao;
import org.bahmni.module.hip.model.PatientCareContext;
import org.junit.Test;
import org.openmrs.api.PatientService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class CareContextServiceTest {
    private final CareContextRepository careContextRepository = mock(CareContextRepository.class);
    private final PatientService patientService = mock(PatientService.class);
    private final ValidationService validationService = mock(ValidationService.class);
    private final ExistingPatientDao existingPatientDao = mock(ExistingPatientDao.class);

    private final CareContextService careContextServiceObject = new CareContextService(careContextRepository, patientService, validationService, existingPatientDao);

    @Test
    public void shouldFetchAllCareContextForPatient() {
        String patientUuid = "c04fa14e-9997-4bfe-80a2-9c474b94dd8a";
        List<PatientCareContext> careContexts = new ArrayList<>();

        when(careContextRepository.getPatientCareContext(patientUuid))
                .thenReturn(careContexts);

        careContextServiceObject.careContextForPatient(patientUuid);

        verify(careContextRepository, times(1))
                .getPatientCareContext(patientUuid);
    }
}
