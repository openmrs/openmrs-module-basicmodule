package org.bahmni.module.hip.api.dao;

import org.openmrs.DrugOrder;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.Date;
import java.util.List;

public interface PrescriptionOrderDao {
    List<DrugOrder> getDrugOrders(Visit visit);
    List<DrugOrder> getDrugOrdersForProgram(Patient patient, Date fromDate, Date toDate, OrderType orderType, String program, String programEnrollmentId);

}
