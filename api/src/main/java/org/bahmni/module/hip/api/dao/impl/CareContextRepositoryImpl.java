package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.CareContextRepository;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.bahmni.module.hip.model.PatientCareContext;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bahmni.module.hip.api.dao.Constants.PROGRAM;
import static org.bahmni.module.hip.api.dao.Constants.VISIT_TYPE;

@Repository
public class CareContextRepositoryImpl implements CareContextRepository {
    private SessionFactory sessionFactory;
    private PatientService  patientService;
    private VisitService visitService;
    private ProgramWorkflowService programWorkflowService;
    private  EncounterDao encounterDao;


    @Autowired
    public CareContextRepositoryImpl(SessionFactory sessionFactory, PatientService patientService, VisitService visitService, ProgramWorkflowService programWorkflowService, EncounterDao encounterDao) {
        this.sessionFactory = sessionFactory;
        this.patientService = patientService;
        this.visitService = visitService;
        this.programWorkflowService = programWorkflowService;
        this.encounterDao = encounterDao;
    }

    @Override
    public List<PatientCareContext> getPatientCareContext(String patientUuid) {
        List<PatientCareContext> careContexts = new ArrayList<>();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Visit> visits = getAllVisitForPatient(patient);
        List<PatientProgram> patientPrograms = getAllPrograms(patient);
        for (Visit visit: visits) {
            careContexts.add(getPatientCareContext(visit));
        }
        for (PatientProgram program: patientPrograms) {
            careContexts.add(getPatientCareContext(program));
        }
        return careContexts;
    }


    @Override
    public List<PatientCareContext> getNewPatientCareContext(Patient patient) {
        List<PatientCareContext> careContexts = new ArrayList<>();
        List<Visit> visits = getAllVisitForPatient(patient);
        List<PatientProgram> patientPrograms = getAllPrograms(patient);
        Visit visit = !visits.isEmpty() ? visits.get(0) : null;
        PatientProgram program = !patientPrograms.isEmpty() ? patientPrograms.get(0) : null;
        if(visit == null && program != null)
            careContexts.add(getPatientCareContext(program));
        else if(visit != null && program == null)
            careContexts.add(getPatientCareContext(visit));
        else if(visit != null && program != null) {
            if (program.getDateCreated().before(visit.getStartDatetime()))
                careContexts.add(getPatientCareContext(visit));
            else
                careContexts.add(getPatientCareContext(program));
        }
        return careContexts;
    }

    private PatientCareContext getPatientCareContext(Visit visit) {
        return new PatientCareContext(VISIT_TYPE,
                visit.getVisitType().getName().concat(" / ").concat(visit.getStartDatetime().toString()),
                visit.getCreator().getPersonName().getFullName());
    }

    private PatientCareContext getPatientCareContext(PatientProgram program) {
        return new PatientCareContext(PROGRAM,
                program.getProgram().getName(),
                getProgramEnrollementId(program.getPatientProgramId()).get(0));
    }

    private List<Integer> getEpisodeIds() {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery("select\n" +
                "\t\tepisode_id\n" +
                "\tfrom\n" +
                "\t\tepisode_encounter\n");
        return query.list();
    }

    private List<String> getProgramEnrollementId(Integer patientProgramId) {
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery("SELECT\n" +
                "    value_reference FROM patient_program_attribute WHERE patient_program_id = :patientProgramId\n");
        query.setParameter("patientProgramId", patientProgramId);
        return query.list();
    }

    private List<Visit> getAllVisitForPatient(Patient patient){
        List<Visit> visits = new ArrayList<>();
        for (Visit visit: visitService.getVisitsByPatient(patient)) {
            Set<Encounter> encounters = visit.getEncounters().stream()
                    .filter(encounter -> !encounterDao.GetEpisodeEncounterIds().contains(encounter.getEncounterId()))
                    .collect(Collectors.toSet());
            if(!encounters.isEmpty())
                visits.add(visit);
        }
        return visits;
    }

    private List<PatientProgram> getAllPrograms(Patient patient){
        List<PatientProgram> programs = new ArrayList<>();
        List<Integer> episodeIds = getEpisodeIds();
        Set<PatientProgram> patientPrograms = new HashSet<>(programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false));
        for (PatientProgram program: patientPrograms) {
            if(episodeIds.contains(program.getId()))
                programs.add(program);
        }
        return programs;
    }

}
