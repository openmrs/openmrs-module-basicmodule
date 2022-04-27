package org.bahmni.module.hip.api.dao;

import java.util.Date;
import java.util.List;

public interface EncounterDao {

    List<Integer> GetEncounterIdsForVisitForPrescriptions(String patientUUID, String visit, Date visitStartDate, Date fromDate, Date toDate) ;
    List<Integer> GetEncounterIdsForProgramForPrescriptions(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate) ;
    List<Integer> GetEncounterIdsForProgramForDiagnosticReport(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate);
    List<Integer> GetEncounterIdsForVisitForDiagnosticReport(String patientUUID, String visit,Date visitStartDate, Date fromDate, Date toDate) ;
}
