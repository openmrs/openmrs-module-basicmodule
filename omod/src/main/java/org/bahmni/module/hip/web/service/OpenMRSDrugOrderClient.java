package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.openmrs.DrugOrder;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OpenMRSDrugOrderClient {

    private PatientService patientService;
    private DrugOrderService drugOrderService;
    private OrderService orderService;
    private PrescriptionOrderDao prescriptionOrderDao;

    @Autowired
    public OpenMRSDrugOrderClient(PatientService patientService, DrugOrderService drugOrderService, OrderService orderService, PrescriptionOrderDao prescriptionOrderDao) {
        this.patientService = patientService;
        this.drugOrderService = drugOrderService;
        this.orderService = orderService;
        this.prescriptionOrderDao = prescriptionOrderDao;
    }

    List<DrugOrder> getDrugOrdersFor(String forPatientUUID, String byTheirVisitType) {

        Patient patient = patientService.getPatientByUuid(forPatientUUID);

        return drugOrderService.getAllDrugOrderFor(patient, byTheirVisitType);
    }

    List<DrugOrder> getDrugOrdersByDateFor(String forPatientUUID, Date fromDate, Date toDate) {
        Patient patient = patientService.getPatientByUuid(forPatientUUID);
        OrderType drugOrderType = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        return prescriptionOrderDao.getDrugOrders(patient, fromDate, toDate, drugOrderType);
    }
}
