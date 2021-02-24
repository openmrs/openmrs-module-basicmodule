package org.bahmni.module.hip.api.dao;

import java.util.Date;
import java.util.List;

public interface EncounterDao {

    public List<Integer> GetEncounterIdsForVisit(String patientUUID, String visit, Date fromDate, Date toDate) ;
    public List<Integer> GetEncounterIdsForProgram(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate) ;

}
