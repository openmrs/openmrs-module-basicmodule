package org.bahmni.module.hip.api.dao;

import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import java.util.Date;
import java.util.List;

public interface ConsultationDao {
    List<Obs> getChiefComplaints(Patient patient, String visit, Date fromDate, Date toDate);
    List<Obs> getPhysicalExamination(Patient patient, String visit, Date fromDate, Date toDate);
    List<Order> getOrders(Patient patient, String visit, Date fromDate, Date toDate);
}
