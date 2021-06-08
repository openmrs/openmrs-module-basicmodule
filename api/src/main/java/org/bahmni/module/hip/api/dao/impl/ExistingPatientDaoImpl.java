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
    private static final int PERSON_PHONE_NUMBER_ATTRIBUTE_TYPE_ID = 15;

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

    @Override
    public String getPhoneNumber(Integer patientId) {
        String getPatientPhoneNumberWithPatientIdQuery =
                "select value from person_attribute where person_id=:patientId and person_attribute_type_id="
                        + PERSON_PHONE_NUMBER_ATTRIBUTE_TYPE_ID + ";";
        Query query = this.sessionFactory.openSession().createSQLQuery(getPatientPhoneNumberWithPatientIdQuery);
        query.setParameter("patientId", patientId);
        List<String> phoneNumbers = query.list();
        return phoneNumbers.size() > 0 ? phoneNumbers.get(0) : null;
    }
}

