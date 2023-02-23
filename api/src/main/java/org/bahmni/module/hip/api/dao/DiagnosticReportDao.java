package org.bahmni.module.hip.api.dao;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Visit;

import java.util.List;
import java.util.Map;

public interface DiagnosticReportDao {
    Map<Encounter,List<Obs>> getAllUnorderedUploadsForVisit(String patientUUID, Visit visit);
    Map<Encounter,List<Obs>> getAllOrderedTestUploads(String patientUUID,Visit visit);
}