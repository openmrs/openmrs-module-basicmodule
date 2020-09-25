package org.bahmni.module.hip.web.service;


import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class OpenMRSDrugOrderClientTest {

    private PatientService patientService = mock(PatientService.class);
    private OrderService orderService = mock(OrderService.class);
    private PrescriptionOrderDao prescriptionOrderDao = mock(PrescriptionOrderDao.class);

    private OpenMRSDrugOrderClient openMRSDrugOrderClient =
            new OpenMRSDrugOrderClient(patientService, orderService, prescriptionOrderDao);

    @Test
    public void shouldFetchPatientByItsUUID() {

        openMRSDrugOrderClient.drugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "");

        verify(patientService).getPatientByUuid("0f90531a-285c-438b-b265-bb3abb4745bd");
    }

    @Test
    public void shouldFetchAllOrdersForPatient() {

        Patient patient = mock(Patient.class);

        when(patientService.getPatientByUuid(anyString()))
                .thenReturn(patient);

        openMRSDrugOrderClient.drugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "");

        verify(orderService, times(1))
                .getAllOrdersByPatient(patient);
    }

    @Test
    public void shouldFilterOutOrdersThatDoNotMatchTheVisitType() {

        Patient patient = mock(Patient.class);

        Order order = mock(Order.class);
        Encounter encounter = mock(Encounter.class);
        Visit visit = mock(Visit.class);
        VisitType visitType = mock(VisitType.class);

        when(patientService.getPatientByUuid(anyString()))
                .thenReturn(patient);
        when(order.getEncounter()).thenReturn(encounter);
        when(encounter.getVisit()).thenReturn(visit);
        when(visit.getVisitType()).thenReturn(visitType);
        when(visitType.getName()).thenReturn("IPD");

        List<Order> orders = new ArrayList<>();

        orders.add(order);

        when(orderService.getAllOrdersByPatient(patient)).thenReturn(orders);

        List<DrugOrder> drugOrders = openMRSDrugOrderClient.drugOrdersFor("", "OPD");

        assertEquals(0, drugOrders.size());
    }

    @Test
    public void shouldFilterInOnlyDrugOrdersThatMatchTheType() {

        Patient patient = mock(Patient.class);

        OrderType orderType = new OrderType(345);
        orderType.setUuid(OrderType.DRUG_ORDER_TYPE_UUID);

        DrugOrder order = mock(DrugOrder.class);
        Encounter encounter = mock(Encounter.class);
        Visit visit = mock(Visit.class);
        VisitType visitType = mock(VisitType.class);

        when(patientService.getPatientByUuid(anyString()))
                .thenReturn(patient);
        when(order.getEncounter()).thenReturn(encounter);
        when(order.getOrderType()).thenReturn(orderType);
        when(encounter.getVisit()).thenReturn(visit);
        when(visit.getVisitType()).thenReturn(visitType);
        when(visitType.getName()).thenReturn("OPD");

        List<Order> orders = new ArrayList<>();
        orders.add(order);

        when(orderService.getAllOrdersByPatient(patient)).thenReturn(orders);

        List<DrugOrder> drugOrders = openMRSDrugOrderClient
                .drugOrdersFor("0f90531a-285c-438b-b265-bb3abb4745bd", "OPD");

        assertEquals(1, drugOrders.size());
    }
}
