package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.EncounterDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EncounterDaoImpl implements EncounterDao {

    private static final String RADIOLOGY_TYPE = "RADIOLOGY";
    private static final String PATIENT_DOCUMENT_TYPE = "Patient Document";
    private static final String DOCUMENT_TYPE = "Document";
    private SessionFactory sessionFactory;

    @Autowired
    public EncounterDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private String sqlGetEpisodeEncounterIds = "select\n" +
            "\t\tencounter_id\n" +
            "\tfrom\n" +
            "\t\tepisode_encounter\n";

    private String sqlGetEncounterIdsForProgramForPrescriptions = "SELECT\n" +
            "  le.encounter_id\n" +
            "FROM\n" +
            "  patient_program AS pp\n" +
            "  INNER JOIN program AS p ON pp.program_id = p.program_id\n" +
            "  INNER JOIN (\n" +
            "    SELECT\n" +
            "      ee.episode_id,\n" +
            "      ee.encounter_id,\n" +
            "      epp.patient_program_id,\n" +
            "      v.date_started\n" +
            "    FROM\n" +
            "      episode_encounter AS ee\n" +
            "      INNER JOIN episode_patient_program AS epp ON ee.episode_id = epp.episode_id\n" +
            "      INNER JOIN encounter AS e ON e.encounter_id = ee.encounter_id\n" +
            "      INNER JOIN visit AS v ON v.visit_id = e.visit_id\n" +
            "  ) AS le ON le.patient_program_id = pp.patient_program_id\n" +
            "WHERE\n" +
            "  p.name = :programName\n" +
            "  AND pp.patient_id = (\n" +
            "    SELECT\n" +
            "      person_id\n" +
            "    FROM\n" +
            "      person AS p2\n" +
            "    WHERE\n" +
            "      p2.uuid = :patientUUID\n" +
            "  )\n" +
            "  AND pp.patient_program_id IN (\n" +
            "    SELECT\n" +
            "      patient_program_id\n" +
            "    FROM\n" +
            "      program_attribute_type AS pat\n" +
            "      INNER JOIN patient_program_attribute AS ppa ON pat.program_attribute_type_id = ppa.attribute_type_id\n" +
            "    WHERE\n" +
            "      name = \"ID_Number\"\n" +
            "      AND value_reference = :programEnrollmentId\n" +
            "  )\n" +
            "  AND le.date_started BETWEEN :fromDate AND :toDate ;\n";

    private String sqlGetEncounterIdsForProgramForDiagnosticReports = "SELECT\n" +
            "  res.encounter_id\n" +
            "FROM\n" +
            "  (\n" +
            "    SELECT\n" +
            "      *\n" +
            "    from(\n" +
            "        SELECT\n" +
            "          o.encounter_id,\n" +
            "          p.uuid AS person_uuid,\n" +
            "          p2.name AS pro_name,\n" +
            "          ppa.value_reference,\n" +
            "          pp.date_enrolled,\n" +
            "          o.concept_id AS obs_concept_id,\n" +
            "          o.value_text,\n" +
            "          o.void_reason AS obs_void_reason\n" +
            "        from\n" +
            "          obs o\n" +
            "          inner join person p on p.person_id = o.person_id\n" +
            "          inner join patient_program pp on pp.patient_id = p.person_id\n" +
            "          inner join program p2 on p2.program_id = pp.program_id\n" +
            "          inner join patient_program_attribute ppa on ppa.patient_program_id = pp.patient_program_id\n" +
            "        where\n" +
            "          encounter_id in (\n" +
            "            SELECT\n" +
            "              encounter_id\n" +
            "            from\n" +
            "              encounter e\n" +
            "              inner join encounter_type as et on et.encounter_type_id = e.encounter_type\n" +
            "            where\n" +
            "              (\n" +
            "                et.name = '" + RADIOLOGY_TYPE + "'\n" +
            "                or et.name = '" + PATIENT_DOCUMENT_TYPE + "'\n" +
            "              )\n" +
            "              and visit_id in (\n" +
            "                SELECT\n" +
            "                  visit_id\n" +
            "                from\n" +
            "                  encounter e2\n" +
            "                  inner join episode_encounter ee on e2.encounter_id = ee.encounter_id\n" +
            "              )\n" +
            "          )\n" +
            "      ) as t\n" +
            "      INNER JOIN concept_name AS cn ON cn.concept_id = t.obs_concept_id\n" +
            "    WHERE\n" +
            "      name = '" + DOCUMENT_TYPE + "'\n" +
            "      and obs_void_reason is null\n" +
            "      and person_uuid = :patientUUID\n" +
            "      and pro_name = :programName\n" +
            "      and value_reference = :programEnrollmentId\n" +
            "  ) as res\n" +
            "  inner join encounter as e on e.encounter_id = res.encounter_id\n" +
            "  inner join visit as v on v.visit_id = e.visit_id\n" +
            "where\n" +
            "  date_started between :fromDate\n" +
            "  and :toDate ;\n";

    @Override
    public List<Integer> GetEpisodeEncounterIds() {

        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetEpisodeEncounterIds);
        return query.list();
    }

    @Override
    public List<Encounter> GetEncountersForVisit(Visit visit, String encounterType) {
        List<Integer> episodeEncounters = GetEpisodeEncounterIds();
        List<Encounter> encounters = visit.getEncounters().stream()
                .filter(encounter -> !episodeEncounters.contains(encounter.getId()))
                .filter(encounter -> Objects.equals(encounter.getEncounterType().getName(), encounterType))
                .collect(Collectors.toList());
        return encounters;
    }

    @Override
    public List<Obs> GetAllObsForVisit(Visit visit, String encounterType, String conceptName) {
        List<Obs> observations = new ArrayList<>();
        List<Encounter> encounters = GetEncountersForVisit(visit,encounterType);
        for (Encounter encounter : encounters) {
            if(conceptName == null)
                observations.addAll(encounter.getAllObs());
            observations.addAll(encounter.getAllObs().stream()
                                .filter(o -> Objects.equals(o.getConcept().getName().getName(), conceptName))
                                .collect(Collectors.toList()));
        }
        return observations;
    }

    @Override
    public List<Order> GetOrdersForVisit(Visit visit) {
        List<Integer> episodeEncounters = GetEpisodeEncounterIds();
        List<Encounter> encounters = visit.getEncounters().stream()
                .filter(encounter -> !episodeEncounters.contains(encounter.getId()))
                .collect(Collectors.toList());
        List<Order> orderList = new ArrayList<>();
        for (Encounter encounter: encounters) {
            orderList.addAll(encounter.getOrders());
        }
        return orderList;
    }

    @Override
    public List<Integer> GetEncounterIdsForProgramForPrescriptions(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetEncounterIdsForProgramForPrescriptions);
        query.setParameter("patientUUID", patientUUID);
        query.setParameter("programName", program);
        query.setParameter("programEnrollmentId", programEnrollmentID);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.list();
    }

    @Override
    public List<Integer> GetEncounterIdsForProgramForDiagnosticReport(String patientUUID, String program, String programEnrollmentID, Date fromDate, Date toDate) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery(sqlGetEncounterIdsForProgramForDiagnosticReports);
        query.setParameter("patientUUID", patientUUID);
        query.setParameter("programName", program);
        query.setParameter("programEnrollmentId", programEnrollmentID);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);

        return query.list();
    }
}
