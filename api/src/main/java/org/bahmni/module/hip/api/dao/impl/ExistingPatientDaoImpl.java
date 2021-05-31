package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.ExistingPatientDao;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExistingPatientDaoImpl implements ExistingPatientDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public ExistingPatientDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getPatientUuidWithHealthId(String healthId) {
        String getPatientWithHealthIdQuery = "SELECT p.uuid FROM person AS p INNER JOIN \n" +
                "\t\t\t\t   patient_identifier AS pi ON p.person_id = pi.patient_id \n" +
                "\t\t\t\t   WHERE identifier = :healthId ;";
        Query query = this.sessionFactory.openSession().createSQLQuery(getPatientWithHealthIdQuery);
        query.setParameter("healthId", healthId);
        List<String> patientUuids =  query.list();
        return patientUuids.size() > 0 ? patientUuids.get(0) : null;
    }

    @Override
    public List<Patient> getPatientsWithPhoneNumber(String phoneNumber) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Patient.class);
        criteria.createCriteria("attributes", "pa")
                .add(Restrictions.eq("pa.value", phoneNumber));
        return criteria.list();
    }
}

