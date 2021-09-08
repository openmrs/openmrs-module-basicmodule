package org.bahmni.module.hip.api.dao;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.Order;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OPConsultDao {
    List<Obs> getChiefComplaints(Patient patient, String visit, Date fromDate, Date toDate);
    Map<Encounter, List<Condition>> getMedicalHistoryConditions(Patient patient, String visit, Date fromDate, Date toDate);
    List<Obs> getMedicalHistoryDiagnosis(Patient patient, String visit, Date fromDate, Date toDate);
    List<Obs> getPhysicalExamination(Patient patient, String visit, Date fromDate, Date toDate);
    List<Obs> getProcedures(Patient patient, String visit, Date fromDate, Date toDate);
    List<Order> getOrders(Patient patient, String visit, Date fromDate, Date toDate);
}
