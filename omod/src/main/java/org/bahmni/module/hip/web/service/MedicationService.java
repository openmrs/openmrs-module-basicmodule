package org.bahmni.module.hip.web.service;

import com.google.gson.Gson;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;

import java.util.List;
import java.util.stream.Collectors;

public class MedicationService {
    public String getMedication(String patientId, String visitType)
    {
        if(patientId == null || patientId.isEmpty() || visitType == null || visitType.isEmpty()){
            return "Patient id and visit type are required.";
        }

        try {
            Patient patient = Context.getPatientService().getPatientByUuid(patientId);
            List<Order> listOrders = getOrdersByVisitType(patient, visitType);
            String ordersToJson = new Gson().toJson(listOrders);
            return ordersToJson;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private List<Order> getOrdersByVisitType(Patient patient, String visitType) {
        List<Order> listOrders = Context.getOrderService().getAllOrdersByPatient(patient);
        return filterOrdersByVisitType(listOrders, visitType);
    }

    private List<Order> filterOrdersByVisitType(List<Order> orders, String visitType) {
        return orders.stream().filter(order -> order.getEncounter().getVisit().getVisitType().getName() == visitType)
                .collect(Collectors.toList());

    }
}
