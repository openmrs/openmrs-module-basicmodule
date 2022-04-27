package org.bahmni.module.hip.api.dao.impl;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.emrapi.conditionslist.ConditionService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class OPConsultDaoImpl implements OPConsultDao {
    public static final String PROCEDURE_NOTES = "Procedure Notes, Procedure";
    public static final String CONSULTATION = "Consultation";
    private static final String CODED_DIAGNOSIS = "Coded Diagnosis";

    private final ObsService obsService;
    private final ConditionService conditionService;
    private final EncounterService encounterService;
    private final ProgramWorkflowService programWorkflowService;
    private final EpisodeService episodeService;


    @Autowired
    public OPConsultDaoImpl(ObsService obsService, ConditionService conditionService, EncounterService encounterService, ProgramWorkflowService programWorkflowService, EpisodeService episodeService) {
        this.obsService = obsService;
        this.conditionService = conditionService;
        this.encounterService = encounterService;
        this.programWorkflowService = programWorkflowService;
        this.episodeService = episodeService;
    }

    @Override
    public Map<Encounter, List<Condition>> getMedicalHistoryConditions(Patient patient, String visit, Date visitStartDate, Date fromDate, Date toDate) {
        final String conditionStatusHistoryOf = "HISTORY_OF";
        final String conditionStatusActive = "ACTIVE";
        List<Encounter> encounters = encounterService.getEncountersByPatient(patient)
                                                     .stream()
                                                     .filter(encounter -> Objects.equals(encounter.getEncounterType().getName(), "Consultation") &&
                                                                         encounter.getDateCreated().after(fromDate) &&
                                                                         encounter.getDateCreated().before(toDate) &&
                                                                         encounter.getVisit().getStartDatetime().getTime() == visitStartDate.getTime() &&
                                                                         Objects.equals(encounter.getVisit().getVisitType().getName(), visit))
                                                     .collect(Collectors.toList());
        List<Condition> conditions = conditionService.getActiveConditions(patient)
                                                     .stream()
                                                     .filter(condition -> condition.getStatus().name().equals(conditionStatusActive) ||
                                                                          condition.getStatus().name().equals(conditionStatusHistoryOf))
                                                     .collect(Collectors.toList());

        Map<Encounter,List<Condition>> encounterConditionsMap = new HashMap<>();

        for(Condition condition : conditions){
            for(Encounter encounter : encounters){
                Encounter nextEncounter;
                Encounter encounterForNxtVisit = encounterService.getEncounter(encounter.getId() + 1);
                Date nextEncounterDate = encounterForNxtVisit != null ? encounterForNxtVisit.getDateCreated() : new Date();
                if(encounters.indexOf(encounter) < (encounters.size() - 1)){
                    nextEncounter = encounterService.getEncounter(encounters.get(encounters.indexOf(encounter)+1).getId());
                    nextEncounterDate = nextEncounter.getDateCreated();
                }
                if (condition.getDateCreated().equals(encounter.getDateCreated()) || condition.getDateCreated().after(encounter.getDateCreated()) && condition.getDateCreated().before(nextEncounterDate)) {
                    if (encounterConditionsMap.containsKey(encounter)) {
                        encounterConditionsMap.get(encounter).add(condition);
                    } else {
                        encounterConditionsMap.put(encounter, new ArrayList<Condition>() {{
                            add(condition);
                        }});
                    }
                }
            }
        }
        return encounterConditionsMap;
    }

    public List<Obs> getAllObsBetweenDates(Patient patient, Date fromDate, Date toDate) {
        List<Obs> patientObs = obsService.getObservationsByPerson(patient)
                .stream()
                .filter(obs -> obs.getEncounter().getVisit().getStartDatetime().after(fromDate)
                        && obs.getEncounter().getVisit().getStartDatetime().before(toDate))
                .collect(Collectors.toList());
        return patientObs;
    }

    @Override
    public List<Obs> getMedicalHistoryDiagnosis(Patient patient, String visit, Date visitStartDate, Date fromDate, Date toDate) {
        List<Obs> patientObs = getAllObsBetweenDates(patient,fromDate,toDate);
        List<Obs> medicalHistoryDiagnosisObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().getTime() == visitStartDate.getTime()
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), visit)
                    && Objects.equals(o.getConcept().getName().getName(), CODED_DIAGNOSIS)
            )
            {
                medicalHistoryDiagnosisObsMap.add(o);
            }
        }
        return medicalHistoryDiagnosisObsMap;
    }

    @Override
    public List<Obs> getProcedures(Patient patient, String visit, Date visitStartDate, Date fromDate, Date toDate) {
        List<Obs> patientObs = getAllObsBetweenDates(patient,fromDate,toDate);
        List<Obs> proceduresObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().getTime() == visitStartDate.getTime()
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), visit)
                    && Objects.equals(o.getConcept().getName().getName(), PROCEDURE_NOTES)
                    && !o.getVoided()
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
                            && !o.getVoided()
                            && Objects.equals(o.getConcept().getName().getName(), PROCEDURE_NOTES)
                    ) {
                        proceduresObsSet.add(o);
                    }
                }
            }
        }
        return proceduresObsSet;
    }

    @Override
    public Map<Encounter, List<Condition>> getMedicalHistoryConditionsForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        final String conditionStatusHistoryOf = "HISTORY_OF";
        final String conditionStatusActive = "ACTIVE";
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient,programWorkflowService.getProgramByName(programName), fromDate, toDate,null,null,false);
        List<Encounter> encounterList = new ArrayList<>();
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        for (PatientProgram patientProgram: patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            encounterList.addAll(episode.getEncounters());
        }
        List<Encounter> encounters = encounterList
                .stream()
                .filter(encounter -> Objects.equals(encounter.getEncounterType().getName(), "Consultation"))
                .collect(Collectors.toList());
        Set<Condition> conditions = conditionService.getActiveConditions(patient)
                .stream()
                .filter(condition -> condition.getStatus().name().equals(conditionStatusActive) ||
                        condition.getStatus().name().equals(conditionStatusHistoryOf))
                .collect(Collectors.toSet());

        Map<Encounter,List<Condition>> encounterConditionsMap = new HashMap<>();

        for(Condition condition : conditions){
            for(Encounter encounter : encounters){
                Encounter nextEncounter;
                Date nextEncounterDate = new Date();
                if(encounters.indexOf(encounter) < (encounters.size() - 1)){
                    nextEncounter = encounterService.getEncounter(encounters.get(encounters.indexOf(encounter)+1).getId());
                    nextEncounterDate = nextEncounter.getDateCreated();
                }
                if(condition.getDateCreated().equals(encounter.getDateCreated()) || condition.getDateCreated().after(encounter.getDateCreated()) && condition.getDateCreated().before(nextEncounterDate)) {
                    if(encounterConditionsMap.containsKey(encounter)) {
                        encounterConditionsMap.get(encounter).add(condition);
                    } else {
                        encounterConditionsMap.put(encounter, new ArrayList<Condition>() {{
                            add(condition);
                        }});
                    }
                }
            }
        }
        return encounterConditionsMap;
    }

    @Override
    public List<Obs> getMedicalHistoryDiagnosisForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient,programWorkflowService.getProgramByName(programName), fromDate, toDate,null,null,false);
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        List<Obs> obsSet= new ArrayList<>();
        for (PatientProgram patientProgram: patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            Set<Encounter> encounterSet = episode.getEncounters();
            for (Encounter encounter: encounterSet) {
                for(Obs o : encounter.getAllObs()){
                    if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                            && Objects.equals(o.getConcept().getName().getName(), CODED_DIAGNOSIS)
                            && o.getValueCoded() != null
                            && o.getConcept().getName().getLocalePreferred())
                    {
                        obsSet.add(o);
                    }
                }
            }
        }
        return obsSet;
    }
}
