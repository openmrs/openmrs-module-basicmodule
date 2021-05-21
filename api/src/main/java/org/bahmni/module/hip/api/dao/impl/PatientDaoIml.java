package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.PatientDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PatientDaoIml implements PatientDao {


    private final SessionFactory sessionFactory;
    private static final int PERSON_PHONE_NUMBER_ATTRIBUTE_TYPE_ID = 15;

    @Autowired
    public PatientDaoIml(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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

