package org.bahmni.module.hip.api.dao;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;

import java.util.List;
import java.util.Map;

public interface DiagnosticReportDao {
    Map<Encounter,List<Obs>> getAllLabReportsForVisit(String patientUUID, Visit visit);
    String getTestNameForLabReports(Obs obs);
}