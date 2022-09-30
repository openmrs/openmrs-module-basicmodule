package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.Config;
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
        List<String> patientUuids = query.list();
        return patientUuids.size() > 0 ? patientUuids.get(0) : null;
    }

    @Override
    public List<Patient> getPatientsWithPhoneNumber(String phoneNumber) {
        Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(Patient.class);
        criteria.createCriteria("attributes", "pa")
                .add(Restrictions.like("pa.value", "%" + phoneNumber));
        return criteria.list();
    }

    @Override
    public String getPhoneNumber(Integer patientId) {
        String getPatientPhoneNumberWithPatientIdQuery =
                " SELECT   value FROM   person_attribute   INNER JOIN person_attribute_type ON" +
                        " person_attribute.person_attribute_type_id = person_attribute_type.person_attribute_type_id " +
                        "where   person_id = :patientId   and name = \"phoneNumber\";";
        Query query = this.sessionFactory.openSession().createSQLQuery(getPatientPhoneNumberWithPatientIdQuery);
        query.setParameter("patientId", patientId);
        List<String> phoneNumbers = query.list();
        return phoneNumbers.size() > 0 ? phoneNumbers.get(0) : null;
    }

    @Override
    public String getPatientHealthIdWithPatientId(Integer patientId) {
        String getPatientHealthId = "select\n" +
                "\tpi.identifier\n" +
                "from\n" +
                "\tpatient_identifier as pi\n" +
                "inner join patient_identifier_type as piy on\n" +
                "\tpi.identifier_type = piy.patient_identifier_type_id\n" +
                "where\n" +
                "\tpi.patient_id = :patientId\n" +
                "\tand piy.name = :healthId ;";
        Query query = this.sessionFactory.openSession().createSQLQuery(getPatientHealthId);
        query.setParameter("patientId", patientId);
        query.setParameter("healthId", Config.ABHA_ADDRESS.getValue());
        List<String> healthIds = query.list();
        return healthIds.size() > 0 ? healthIds.get(0) : null;
    }
}