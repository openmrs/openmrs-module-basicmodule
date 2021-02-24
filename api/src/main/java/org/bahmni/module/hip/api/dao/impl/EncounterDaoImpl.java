package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.EncounterDao;
import org.bahmni.module.hip.model.PatientCareContext;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.openmrs.logic.op.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class EncounterDaoImpl implements EncounterDao {

    private SessionFactory sessionFactory;

    @Autowired
    public EncounterDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private String sqlGetEncounterIdsForVisit = "select\n" +
            "\te.encounter_id\n" +
            "from\n" +
            "\tvisit as v\n" +
            "inner join visit_type as vt on\n" +
            "\tvt.visit_type_id = v.visit_type_id\n" +
            "inner join encounter as e on\n" +
            "\te.visit_id = v.visit_id\n" +
            "where\n" +
            "\tvt.name = :visit\n" +
            "\tand e.encounter_id not in (\n" +
            "\tselect\n" +
            "\t\tencounter_id\n" +
            "\tfrom\n" +
            "\t\tepisode_encounter)\n" +
            "\tand v.date_started between :fromDate and :toDate\n" +
            "\tand v.patient_id = (select person_id from person as p2 where p2.uuid = :patientUUID);";

    private String sqlGetEncounterIdsForProgram = "select\n" +
            "\tle.encounter_id\n" +
            "from\n" +
            "\tpatient_program as pp\n" +
            "inner join program as p on\n" +
            "\tpp.program_id = p.program_id\n" +
            "inner join (\n" +
            "\tselect\n" +
            "\t\tee.episode_id,\n" +
            "\t\tee.encounter_id,\n" +
            "\t\tepp.patient_program_id\n" +
            "\tfrom\n" +
            "\t\tepisode_encounter as ee\n" +
            "\tinner join episode_patient_program as epp on\n" +
            "\t\tee.episode_id = epp.episode_id) as le on\n" +
            "\tle.patient_program_id = pp.patient_program_id\n" +
            "where\n" +
            "\tp.name = :programName \n" +
            "\tand pp.patient_id = (select person_id from person as p2 where p2.uuid = :patientUUID)\n" +
            "\tand pp.patient_program_id in (\n" +
            "\tselect\n" +
            "\t\tpatient_program_id\n" +
            "\tfrom\n" +
            "\t\tprogram_attribute_type as pat\n" +
            "\tinner join patient_program_attribute as ppa on\n" +
            "\t\tpat.program_attribute_type_id = ppa.attribute_type_id\n" +
            "\twhere\n" +
            "\t\tname = \"ID_Number\"\n" +
            "\t\tand value_reference = :programEnrollmentId )\n" +
            "\tand pp.date_enrolled between :fromDate and :toDate ;";

    @Override
    public List<Integer> GetEncounterIdsForVisit(String patientUUID, String visit, Date fromDate, Date toDate) {

        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetEncounterIdsForVisit);
        query.setParameter("patientUUID", patientUUID);
        query.setParameter("visit", visit);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.list();

    }

    @Override
    public List<Integer> GetEncounterIdsForProgram(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetEncounterIdsForProgram);
        query.setParameter("patientUUID", patientUUID);
        query.setParameter("programName", program);
        query.setParameter("programEnrollmentId", programEnrollmentID);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.list();
    }

}
