package org.bahmni.module.hip.api.dao;

import java.util.Date;
import java.util.List;

public interface HipVisitDao {

    List<Integer> GetVisitIdsForProgramForLabResults(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate);
    List<Integer> GetVisitIdsForVisitForLabResults(String patientUUID, String visit, Date fromDate, Date toDate) ;

}
