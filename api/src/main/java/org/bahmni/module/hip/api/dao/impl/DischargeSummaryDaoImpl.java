package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.DischargeSummaryDao;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.ObsService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class DischargeSummaryDaoImpl implements DischargeSummaryDao {

    public static final String CONSULTATION = "Consultation";
    public static final String PROCEDURE_NOTES = "Procedure Notes";
    private final ObsService obsService;
    private final ProgramWorkflowService programWorkflowService;
    private final EpisodeService episodeService;

    @Autowired
    public DischargeSummaryDaoImpl(ObsService obsService, ProgramWorkflowService programWorkflowService, EpisodeService episodeService) {
        this.obsService = obsService;
        this.programWorkflowService = programWorkflowService;
        this.episodeService = episodeService;
    }

    private boolean matchesVisitType(String visitType, Obs obs) {
        return obs.getEncounter().getVisit().getVisitType().getName().equals(visitType);
    }

    @Override
    public List<Obs> getCarePlan(Patient patient, String visit, Date visitStartDate, Date fromDate, Date toDate) {
        final String obsName = "Discharge Summary";
        List<Obs> patientObs = getAllObsBetweenDates(patient,fromDate,toDate);
        List<Obs> carePlanObs = patientObs.stream().filter(obs -> matchesVisitType(visit, obs))
                .filter(obs -> obs.getEncounter().getVisit().getStartDatetime().getTime() == visitStartDate.getTime())
                .filter(obs -> obsName.equals(obs.getConcept().getName().getName()))
                .filter(obs -> obs.getConcept().getName().getLocalePreferred())
                .collect(Collectors.toList());

        return carePlanObs;
    }

    @Override
    public List<Obs> getCarePlanForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        final String obsName = "Discharge Summary";
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient,programWorkflowService.getProgramByName(programName), fromDate, toDate,null,null,false);
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        List<Obs> carePlanObs= new ArrayList<>();
        for (PatientProgram patientProgram: patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            Set<Encounter> encounterSet = episode.getEncounters();
            for (Encounter encounter : encounterSet) {
                for (Obs o : encounter.getAllObs()) {
                    if (obsName.equals(o.getConcept().getName().getName())
                            &&  o.getConcept().getName().getLocalePreferred()) {
                        carePlanObs.add(o);
                    }
                }
            }
        }
        return carePlanObs;
    }

    @Override
    public List<Obs> getProcedures(Patient patient, String visit, Date visitStartDate, Date fromDate, Date toDate) {
        List<Obs> patientObs = getAllObsBetweenDates(patient,fromDate,toDate);
        List<Obs> proceduresObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().getTime() == visitStartDate.getTime()
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), visit)
                    && o.getObsGroup() == null
                    && Objects.equals(o.getConcept().getName().getName(), PROCEDURE_NOTES)
            )
            {
                proceduresObsMap.add(o);
            }
        }
        return proceduresObsMap;
    }

    @Override
    public List<Obs> getProceduresForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient,programWorkflowService.getProgramByName(programName), fromDate, toDate,null,null,false);
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        List<Obs> proceduresObsSet= new ArrayList<>();
        for (PatientProgram patientProgram: patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            Set<Encounter> encounterSet = episode.getEncounters();
            for (Encounter encounter : encounterSet) {
                for (Obs o : encounter.getAllObs()) {
                    if (Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                            && o.getObsGroup() == null
                            && Objects.equals(o.getConcept().getName().getName(), PROCEDURE_NOTES)
                    ) {
                        proceduresObsSet.add(o);
                    }
                }
            }
        }
        return proceduresObsSet;
    }

    public List<Obs> getAllObsBetweenDates(Patient patient, Date fromDate, Date toDate) {
        List<Obs> patientObs = obsService.getObservationsByPerson(patient)
                .stream()
                .filter(obs -> obs.getEncounter().getVisit().getStartDatetime().after(fromDate)
                        && obs.getEncounter().getVisit().getStartDatetime().before(toDate))
                .collect(Collectors.toList());
        return patientObs;
    }
}