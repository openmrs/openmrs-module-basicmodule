package org.bahmni.module.hip.api.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class PrescriptionOrderDaoImpl implements PrescriptionOrderDao {
    protected static final Log log = LogFactory.getLog(PrescriptionOrderDaoImpl.class);
    private SessionFactory sessionFactory;
    private EncounterDao encounterDao;
    private final OrderService orderService;

    @Autowired
    public PrescriptionOrderDaoImpl(SessionFactory sessionFactory, EncounterDao encounterDao, OrderService orderService) {
        this.sessionFactory = sessionFactory;
        this.encounterDao = encounterDao;
        this.orderService = orderService;
    }

    public List<DrugOrder> getDrugOrders(Patient patient,String visitType, Date visitStartDate) {

        Integer [] encounterIds = encounterDao.GetEncounterIdsForVisitForPrescriptions(patient.getUuid(), visitType,visitStartDate).toArray(new Integer[0]);
        if(encounterIds.length == 0)
            return new ArrayList< DrugOrder > ();

        List<DrugOrder> orderLists = orderService.getAllOrdersByPatient(patient).stream()
                .filter(order ->  Arrays.asList(encounterIds).contains(order.getEncounter().getId())
                          && order.getEncounter().getVisit().getVisitType().getName().equals(visitType)
                          && order.getOrderType().getUuid().equals(OrderType.DRUG_ORDER_TYPE_UUID))
                .map(order -> (DrugOrder) order)
                .collect(Collectors.toList());

        return orderLists;
    }

    public List<DrugOrder> getDrugOrdersForProgram(Patient patient, Date fromDate, Date toDate, OrderType orderType, String program, String programEnrollmentId) {

        Integer [] encounterIds = encounterDao.GetEncounterIdsForProgramForPrescriptions(patient.getUuid(), program, programEnrollmentId, fromDate, toDate).toArray(new Integer[0]);
        if(encounterIds.length == 0)
            return new ArrayList< DrugOrder > ();
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Order.class);
        criteria.createCriteria("encounter", "e")
                .createCriteria("visit", "v");
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("orderType", orderType));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.in( "e.encounterId", encounterIds));
        criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
        return criteria.list();
    }

}
