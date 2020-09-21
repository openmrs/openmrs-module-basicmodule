package org.bahmni.module.hip.api.dao;

import org.openmrs.DrugOrder;
import org.openmrs.OrderType;
import org.openmrs.Patient;

import java.util.Date;
import java.util.List;

public interface PrescriptionOrderDao {
    public List<DrugOrder> getDrugOrders(Patient patient, Date fromDate, Date toDate, OrderType orderType);
}
