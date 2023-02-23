package org.bahmni.module.hip.api.dao;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.Date;
import java.util.List;

public interface DischargeSummaryDao {
    List<Obs> getCarePlan(Visit visit);
    List<Obs> getProcedures(Visit visit);
    List<Obs> getProceduresForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    List<Obs> getCarePlanForProgram(String programName, Date fromDate, Date toDate, Patient patient);
}