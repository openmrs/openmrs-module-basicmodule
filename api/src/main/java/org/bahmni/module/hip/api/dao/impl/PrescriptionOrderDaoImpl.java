package org.bahmni.module.hip.api.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class PrescriptionOrderDaoImpl implements PrescriptionOrderDao {
    protected static final Log log = LogFactory.getLog(PrescriptionOrderDaoImpl.class);
    private SessionFactory sessionFactory;

    @Autowired
    public PrescriptionOrderDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<DrugOrder> getDrugOrders(Patient patient, Date fromDate, Date toDate, OrderType orderType) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(DrugOrder.class);
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("orderType", orderType));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
        return criteria.list();
    }

    public List<DrugOrder> getDrugOrders(Patient patient, Date fromDate, Date toDate) {
        Query query = sessionFactory.getCurrentSession().createQuery(
        "select d1 from DrugOrder d1, Encounter e, Visit v where d1.encounter = e and e.visit = v " +
            "and d1.voided = false " +
            "and d1.dateCreated > :fromDate and d1.dateCreated <= :toDate " +
            "and not exists (select d2 from DrugOrder d2 where d2.voided = false and d2.action = :revised " +
                                " and d2.encounter = d1.encounter and d2.previousOrder = d1) " +
            "order by d1.dateActivated desc");
        query.setParameter("revised", Order.Action.REVISE);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        return (List<DrugOrder>) query.list();
    }

}
