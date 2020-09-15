package org.bahmni.module.hip.web.service;

import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenMRSDrugOrderClient {

    private PatientService patientService;
    private OrderService orderService;

    @Autowired
    public OpenMRSDrugOrderClient(PatientService patientService, OrderService orderService) {
        this.patientService = patientService;
        this.orderService = orderService;
    }

    List<DrugOrder> getDrugOrdersFor(String forPatientUUID, String byVisitType) {

        Patient patient = this.patientService.getPatientByUuid(forPatientUUID);
        List<Order> ordersByVisitType = ordersFor(patient, byVisitType);

        return ordersByVisitType
                .stream()
                .filter(this::isDrugOrder)
                .map(order -> (DrugOrder) order)
                .collect(Collectors.toList());
    }

    private boolean isDrugOrder(Order order) {
        return order.getOrderType().getUuid().equals(OrderType.DRUG_ORDER_TYPE_UUID);
    }

    private List<Order> ordersFor(Patient patient, String visitType) {
        List<Order> listOrders = orderService.getAllOrdersByPatient(patient);
        return listOrders.stream()
                .filter(order -> matchVisitType(visitType, order))
                .collect(Collectors.toList());

    }

    private boolean matchVisitType(String visitType, Order order) {
        return order.getEncounter().getVisit().getVisitType().getName().equals(visitType);
    }
}
