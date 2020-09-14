package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.exception.NoMedicationFoundException;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.r4.model.Quantity;
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
public class MedicationService {

    private PatientService patientService;
    private OrderService orderService;

    @Autowired
    public MedicationService(PatientService patientService, OrderService orderService) {
        this.patientService = patientService;
        this.orderService = orderService;
    }

    public List<MedicationRequest> getMedication(String patientId, String visitType) {

        if (patientId == null || patientId.isEmpty() || visitType == null || visitType.isEmpty())
            throw new NoMedicationFoundException(123);


        Patient patient = this.patientService.getPatientByUuid(patientId);
        List<Order> listOrders = getOrdersByVisitType(patient, visitType);

        if (listOrders.isEmpty())
            throw new NoMedicationFoundException(patient.getId());

        return listOrders
                .stream()
                .filter(order -> order.getOrderType().getUuid().equals(OrderType.DRUG_ORDER_TYPE_UUID))
                .map(order -> (DrugOrder) order)
                .map(drugOrder -> {
                    MedicationRequestDispenseRequestComponent medicationRequestDispenseRequestComponent =
                            new MedicationRequestDispenseRequestComponent();

                    medicationRequestDispenseRequestComponent.setQuantity(new Quantity(drugOrder.getQuantity()));

                    MedicationRequest medicationRequest = new MedicationRequest();
                    medicationRequest.setDispenseRequest(medicationRequestDispenseRequestComponent);
                    return medicationRequest;
                })
                .collect(Collectors.toList());
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
