package org.bahmni.module.hip.web.service;

import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.OrderService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class DrugOrderServiceTest {

    private OrderService orderService = mock(OrderService.class);

    private DrugOrderService drugOrderService = new DrugOrderService(orderService);

    @Test
    public void shouldFetchAllOrdersForPatient() {

        Patient patient = mock(Patient.class);

        drugOrderService.getAllDrugOrderFor(patient, "");

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

        when(order.getEncounter()).thenReturn(encounter);
        when(encounter.getVisit()).thenReturn(visit);
        when(visit.getVisitType()).thenReturn(visitType);
        when(visitType.getName()).thenReturn("IPD");

        List<Order> orders = new ArrayList<>();

        orders.add(order);

        when(orderService.getAllOrdersByPatient(patient)).thenReturn(orders);

        List<DrugOrder> drugOrders = drugOrderService.getAllDrugOrderFor(patient, "OPD");

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

        when(order.getEncounter()).thenReturn(encounter);
        when(order.getOrderType()).thenReturn(orderType);
        when(encounter.getVisit()).thenReturn(visit);
        when(visit.getVisitType()).thenReturn(visitType);
        when(visitType.getName()).thenReturn("OPD");

        List<Order> orders = new ArrayList<>();
        orders.add(order);

        when(orderService.getAllOrdersByPatient(patient)).thenReturn(orders);

        List<DrugOrder> drugOrders = drugOrderService.getAllDrugOrderFor(patient, "OPD");

        assertEquals(1, drugOrders.size());
    }
}
