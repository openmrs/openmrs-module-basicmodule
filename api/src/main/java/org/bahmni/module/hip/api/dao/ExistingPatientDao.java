package org.bahmni.module.hip.api.dao;

public interface ExistingPatientDao {
    String getPatientUuidWithHealthId(String healthId);
}
