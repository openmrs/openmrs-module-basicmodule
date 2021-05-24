package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.ExistingPatientDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ExistingPatientDaoImpl implements ExistingPatientDao {

    public static final int HEALTH_ID_IDENTIFIER_TYPE_ID = 5;

    private SessionFactory sessionFactory;

    @Autowired
    public ExistingPatientDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public String getPatientUuidWithHealthId(String healthId) {
        String getPatientWithHealthIdQuery = "SELECT p.uuid FROM person AS p INNER JOIN \n" +
                "\t\t\t\t   patient_identifier AS pi ON p.person_id = pi.patient_id \n" +
                "\t\t\t\t   WHERE pi.identifier_type = " + HEALTH_ID_IDENTIFIER_TYPE_ID + " AND identifier = :healthId ;";
        Query query = this.sessionFactory.openSession().createSQLQuery(getPatientWithHealthIdQuery);
        query.setParameter("healthId", healthId);
        List<String> patientUuids =  query.list();
        return patientUuids.size() > 0 ? patientUuids.get(0) : null;
    }
}
