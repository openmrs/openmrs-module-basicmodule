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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class PrescriptionOrderDaoImpl implements PrescriptionOrderDao {
    protected static final Log log = LogFactory.getLog(PrescriptionOrderDaoImpl.class);
    private SessionFactory sessionFactory;
    private EncounterDao encounterDao;

    @Autowired
    public PrescriptionOrderDaoImpl(SessionFactory sessionFactory, EncounterDao encounterDao) {
        this.sessionFactory = sessionFactory;
        this.encounterDao = encounterDao;
    }

    public List<DrugOrder> getDrugOrders(Patient patient, Date fromDate, Date toDate, OrderType orderType, String visitType, Date visitStartDate) {

        Integer [] encounterIds = encounterDao.GetEncounterIdsForVisitForPrescriptions(patient.getUuid(), visitType, fromDate, toDate).toArray(new Integer[0]);
        if(encounterIds.length == 0)
            return new ArrayList< DrugOrder > ();
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Order.class);
        criteria.createCriteria("encounter", "e")
                .createCriteria("visit", "v")
                .createCriteria("visitType", "vt")
                .add(Restrictions.eq("name", visitType));
        criteria.add(Restrictions.between("date_started", visitStartDate, LocalDate.parse(visitStartDate.toString()).plusDays(1)));
        criteria.add(Restrictions.eq("patient", patient));
        criteria.add(Restrictions.eq("orderType", orderType));
        criteria.add(Restrictions.eq("voided", false));
        criteria.add(Restrictions.in( "e.encounterId", encounterIds));
        criteria.add(Restrictions.between("dateCreated", fromDate, toDate));
        return criteria.list();
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
