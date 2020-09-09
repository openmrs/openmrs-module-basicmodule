package org.bahmni.module.hip.web.service;

import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationService {

    private PatientService patientService;
    private OrderService orderService;

    @Autowired
    public MedicationService(PatientService patientService, OrderService orderService) {
        this.patientService = patientService;
        this.orderService = orderService;
    }

    public String getMedication(String patientId, String visitType) {
        if (patientId == null || patientId.isEmpty() || visitType == null || visitType.isEmpty()) {
            return "Patient id and visit type are required.";
        }

        try {
            Patient patient = this.patientService.getPatientByUuid(patientId);
            List<Order> listOrders = getOrdersByVisitType(patient, visitType);
            if (listOrders.size() == 0)
                return "no medication found!";
            String ordersToJson = listOrders.stream().map(order -> order.getUuid()).findFirst().get();
            return ordersToJson;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private List<Order> getOrdersByVisitType(Patient patient, String visitType) {
        List<Order> listOrders = orderService.getAllOrdersByPatient(patient);
        return filterOrdersByVisitType(listOrders, visitType);
    }

    private List<Order> filterOrdersByVisitType(List<Order> orders, String visitType) {
        return orders.stream()
                .filter(order -> order.getEncounter().getVisit().getVisitType().getName().equals(visitType))
                .collect(Collectors.toList());

    }
}
