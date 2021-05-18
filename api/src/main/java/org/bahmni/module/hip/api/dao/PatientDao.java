package org.bahmni.module.hip.api.dao;

import org.openmrs.Patient;

public interface PatientDao {
    String getPatientUuidWithHealthId(String healthId);
}
