package org.bahmni.module.hip.api.dao;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.conditionslist.Condition;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OPConsultDao {
    Map<Encounter, List<Condition>> getMedicalHistoryConditions(Visit visit);
    List<Obs> getMedicalHistoryDiagnosis(Visit visit);
    List<Obs> getProcedures(Visit visit);
    List<Obs> getProceduresForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    Map<Encounter, List<Condition>> getMedicalHistoryConditionsForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    List<Obs> getMedicalHistoryDiagnosisForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    Map<Encounter, List<Obs>> getPatientDocumentsForVisit(Visit visit);
}
