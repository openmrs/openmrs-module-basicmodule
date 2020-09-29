package org.bahmni.module.hip.api.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
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

    public List<DrugOrder> getDrugOrders(Patient patient, Date fromDate, Date toDate, OrderType orderType, String visitType) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Order.class);
        criteria.createCriteria("encounter", "e")
                .createCriteria("visit", "v")
                .createCriteria("visitType", "vt")
                .add(Restrictions.eq("name", visitType));
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("orderType", orderType));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
        return criteria.list();
    }

}
