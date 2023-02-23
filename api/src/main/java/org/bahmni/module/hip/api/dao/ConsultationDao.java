package org.bahmni.module.hip.api.dao;

import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.Date;
import java.util.List;

public interface ConsultationDao {
    List<Obs> getChiefComplaints(Visit visit);
    List<Obs> getChiefComplaintForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    List<Obs> getPhysicalExamination(Visit visit);
    List<Order> getOrders(Visit visit);
    List<Order> getOrdersForProgram(String programName, Date fromDate, Date toDate, Patient patient);
    List<Obs> getPhysicalExaminationForProgram(String programName, Date fromDate, Date toDate, Patient patient);
}
