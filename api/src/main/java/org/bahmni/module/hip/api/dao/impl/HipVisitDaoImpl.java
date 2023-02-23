package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.HipVisitDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class HipVisitDaoImpl implements HipVisitDao {

    private SessionFactory sessionFactory;
    private VisitService visitService;

    @Autowired
    public HipVisitDaoImpl(SessionFactory sessionFactory, VisitService visitService) {
        this.sessionFactory = sessionFactory;
        this.visitService = visitService;
    }

    private String sqlGetVisitIdsForVisitForLabResults =
            "select distinct e.visit_id\n" +
                    "from visit as v join visit_type as vt on v.visit_type_id = vt.visit_type_id\n" +
                    "join encounter e on  e.visit_id = v.visit_id\n" +
                    "where \n" +
                    " vt.name = :visit \n" +
                    " and v.date_started = :visitStartDate \n" +
                    " and e.visit_id not in (select e1.visit_id from encounter as e1 inner join episode_encounter on episode_encounter.encounter_id = e1.encounter_id) \n" +
                    "and v.patient_id in (select person_id from person as p2 where p2.uuid = :patientUUID) ;" ;

    private String sqlGetVisitIdsForProgramForLabResults = "\n" +
            "select distinct e.visit_id from encounter as e, patient_program_attribute as ppa, visit as v, patient_program pp, program p where \n" +
            "p.name = :programName and \n" +
            "pp.program_id = p.program_id and pp.patient_id = v.patient_id\n" +
            "and ppa.value_reference = :programEnrollmentId and ppa.attribute_type_id = 1 and\n" +
            " v.date_started between :fromDate and :toDate and\n" +
            "e.visit_id in (select e1.visit_id from encounter as e1 inner join episode_encounter on episode_encounter.encounter_id = e1.encounter_id) \n" +
            "and v.patient_id in (select person_id from person as p2 where p2.uuid = :patientUUID) ;";



    @Override
    public List<Integer> GetVisitIdsForProgramForLabResults(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetVisitIdsForProgramForLabResults);
        query.setParameter("patientUUID", patientUUID);
        query.setParameter("programName", program);
        query.setParameter("programEnrollmentId", programEnrollmentID);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.list();
    }

    @Override
    public List<Integer> GetVisitIdsForVisitForLabResults(String patientUUID, String visit, Date visitStartDate) {

        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetVisitIdsForVisitForLabResults);
        query.setParameter("patientUUID", patientUUID);
        query.setParameter("visit", visit);
        query.setParameter("visitStartDate",new java.sql.Timestamp(visitStartDate.getTime()));
        return query.list();
    }

    @Override
    public Visit getPatientVisit(Patient patient, String visitType, Date visitStartDate){
        Visit visit = visitService.getVisitsByPatient(patient)
                .stream().filter(obj -> obj.getStartDatetime().getTime() == visitStartDate.getTime())
                .filter(obj -> obj.getVisitType().getName().equals(visitType)).collect(Collectors.toList()).get(0);
        return visit;
    }
}
