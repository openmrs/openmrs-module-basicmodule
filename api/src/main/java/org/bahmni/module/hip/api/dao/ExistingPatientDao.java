package org.bahmni.module.hip.api.dao;

import org.openmrs.Patient;

import java.util.List;

public interface ExistingPatientDao {
    String getPatientUuidWithHealthId(String healthId);

    List<Patient> getPatientsWithPhoneNumber(String phoneNumber);

    String getPhoneNumber(Integer patientId);

    String getPatientHealthIdWithPatientId(Integer patientId);

}
