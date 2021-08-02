package org.bahmni.module.hip.api.dao;

import org.bahmni.module.hip.model.PatientCareContext;

import java.util.List;

public interface CareContextRepository {
    List<PatientCareContext> getPatientCareContext(String patientUuid);

    List<PatientCareContext> getNewPatientCareContext(Integer patientId);

}
