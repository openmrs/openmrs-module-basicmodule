package org.bahmni.module.hip.api.dao;

import org.bahmni.module.hip.model.PatientCareContext;

import java.util.List;

public interface CareContextRepository {
    public List<PatientCareContext> getPatientCareContext(String patientId);
}
