package org.bahmni.module.hip.api.dao;

import org.openmrs.Obs;
import org.openmrs.Patient;
import java.util.Date;
import java.util.List;

public interface DischargeSummaryDao {
    List<Obs> getCarePlan(Patient patient, String visit, Date fromDate, Date toDate);
    List<Obs> getProcedures(Patient patient, String visit, Date fromDate, Date toDate);
    List<Obs> getProceduresForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    List<Obs> getCarePlanForProgram(String programName, Date fromDate, Date toDate, Patient patient);
}