package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.api.dao.DischargeSummaryDao;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
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

    private final ObsService obsService;
    private final ProgramWorkflowService programWorkflowService;
    private final EpisodeService episodeService;
    private final EncounterDao encounterDao;

    @Autowired
    public DischargeSummaryDaoImpl(ObsService obsService, ProgramWorkflowService programWorkflowService, EpisodeService episodeService, EncounterDao encounterDao) {
        this.obsService = obsService;
        this.programWorkflowService = programWorkflowService;
        this.episodeService = episodeService;
        this.encounterDao = encounterDao;
    }

    @Override
    public List<Obs> getCarePlan(Visit visit) {
        List<Obs> carePlanObs = encounterDao.GetAllObsForVisit(visit, Config.CONSULTATION.getValue(), Config.DISCHARGE_SUMMARY.getValue()).stream()
                .filter(obs -> obs.getConcept().getName().getLocalePreferred())
                .collect(Collectors.toList());

        return carePlanObs;
    }

    @Override
    public List<Obs> getCarePlanForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient,programWorkflowService.getProgramByName(programName), fromDate, toDate,null,null,false);
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        List<Obs> carePlanObs= new ArrayList<>();
        for (PatientProgram patientProgram: patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            Set<Encounter> encounterSet = episode.getEncounters();
            for (Encounter encounter : encounterSet) {
                for (Obs o : encounter.getAllObs()) {
                    if (Config.DISCHARGE_SUMMARY.getValue().equals(o.getConcept().getName().getName())
                            &&  o.getConcept().getName().getLocalePreferred()) {
                        carePlanObs.add(o);
                    }
                }
            }
        }
        return carePlanObs;
    }

    @Override
    public List<Obs> getProcedures(Visit visit) {
        List<Obs> proceduresObsMap = encounterDao.GetAllObsForVisit(visit,Config.CONSULTATION.getValue(), Config.PROCEDURE_NOTES.getValue()).stream()
                .filter(obs -> obs.getObsGroup() == null)
                .collect(Collectors.toList());
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
                    if (Objects.equals(o.getEncounter().getEncounterType().getName(), Config.CONSULTATION.getValue())
                            && o.getObsGroup() == null
                            && Objects.equals(o.getConcept().getName().getName(), Config.PROCEDURE_NOTES.getValue())
                    ) {
                        proceduresObsSet.add(o);
                    }
                }
            }
        }
        return proceduresObsSet;
    }
}