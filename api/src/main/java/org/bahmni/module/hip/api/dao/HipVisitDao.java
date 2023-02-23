package org.bahmni.module.hip.api.dao;

import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.Date;
import java.util.List;

public interface HipVisitDao {

    List<Integer> GetVisitIdsForProgramForLabResults(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate);
    List<Integer> GetVisitIdsForVisitForLabResults(String patientUUID, String visit, Date visitStartDate) ;
    Visit getPatientVisit(Patient patient, String visitType, Date visitStartDate);
}
