package org.bahmni.module.hip.api.dao.impl;
import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.openmrs.Concept;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.openmrs.api.ConditionService;

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

    private final ObsService obsService;
    private final ConditionService conditionService;
    private final EncounterService encounterService;
    private final ProgramWorkflowService programWorkflowService;
    private final EpisodeService episodeService;
    private final EncounterDao encounterDao;


    @Autowired
    public OPConsultDaoImpl(ObsService obsService, ConditionService conditionService, EncounterService encounterService, ProgramWorkflowService programWorkflowService, EpisodeService episodeService, EncounterDao encounterDao) {
        this.obsService = obsService;
        this.conditionService = conditionService;
        this.encounterService = encounterService;
        this.programWorkflowService = programWorkflowService;
        this.episodeService = episodeService;
        this.encounterDao = encounterDao;
    }


    @Override
    public Map<Encounter, List<Condition>> getMedicalHistoryConditions(Visit visit) {
        final String conditionStatusHistoryOf = "HISTORY_OF";
        final String conditionStatusActive = "ACTIVE";
        List<Encounter> encounters = encounterDao.GetEncountersForVisit(visit, Config.CONSULTATION.getValue());
        if(encounters.size() == 0)
            return new HashMap<>();
        List<org.openmrs.Condition> conditions = conditionService.getActiveConditions(visit.getPatient())
                                                     .stream()
                                                     .filter(condition -> condition.getClinicalStatus().name().equals(conditionStatusActive) ||
                                                                          condition.getClinicalStatus().name().equals(conditionStatusHistoryOf))

                                                     .collect(Collectors.toList());
        List<org.openmrs.module.emrapi.conditionslist.Condition> emrapiconditions = new ArrayList<>();
        for(org.openmrs.Condition condition : conditions){
            org.openmrs.module.emrapi.conditionslist.Condition emrapicondition = convertCoreConditionToEmrapiCondition(condition);
            emrapiconditions.add(emrapicondition);
        }

        Map<Encounter,List<Condition>> encounterConditionsMap = new HashMap<>();
        List<Encounter> nextEncounters = encounterService.getEncountersByPatient(visit.getPatient()).stream().filter(e ->
                encounters.get(encounters.size()-1).getId() < e.getId()
        ).collect(Collectors.toList());
        for(Condition condition : emrapiconditions) {
            for (Encounter encounter: encounters) {
                Encounter nextEncounter;
                Date nextEncounterDate = nextEncounters.size() != 0 ? nextEncounters.get(0).getDateCreated() : new Date();
                if(encounters.indexOf(encounter) < (encounters.size() - 1)){
                    nextEncounter = encounterService.getEncounter(encounters.get(encounters.indexOf(encounter)+1).getId());
                    nextEncounterDate = nextEncounter.getDateCreated();
                }
                if(condition.getDateCreated().equals(encounter.getDateCreated()) || (condition.getDateCreated().before(nextEncounterDate) && condition.getDateCreated().after(encounter.getDateCreated()))){
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


    @Override
    public List<Obs> getMedicalHistoryDiagnosis(Visit visit) {
        List<Obs> medicalHistoryDiagnosisObsMap = encounterDao.GetAllObsForVisit(visit, Config.CONSULTATION.getValue(), Config.CODED_DIAGNOSIS.getValue());
        medicalHistoryDiagnosisObsMap.addAll(encounterDao.GetAllObsForVisit(visit, Config.CONSULTATION.getValue(), Config.NON_CODED_DIAGNOSIS.getValue()));
        return medicalHistoryDiagnosisObsMap;
    }

    @Override
    public List<Obs> getProcedures(Visit visit) {
        List<Obs> proceduresObsMap = encounterDao.GetAllObsForVisit(visit, Config.CONSULTATION.getValue(), Config.PROCEDURE_NOTES.getValue()).stream()
                .filter(o -> !o.getVoided())
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
                            && !o.getVoided()
                            && Objects.equals(o.getConcept().getName().getName(), Config.PROCEDURE_NOTES.getValue())
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
        List<org.openmrs.Condition> conditions = conditionService.getActiveConditions(patient)
                .stream()
                .filter(condition -> condition.getClinicalStatus().name().equals(conditionStatusActive) ||
                        condition.getClinicalStatus().name().equals(conditionStatusHistoryOf))
                .collect(Collectors.toList());

        List<org.openmrs.module.emrapi.conditionslist.Condition> emrapiconditions = new ArrayList<>();
        for(org.openmrs.Condition condition : conditions){
            org.openmrs.module.emrapi.conditionslist.Condition emrapicondition = convertCoreConditionToEmrapiCondition(condition);
            emrapiconditions.add(emrapicondition);
        }

        Map<Encounter,List<Condition>> encounterConditionsMap = new HashMap<>();

        for(Condition condition : emrapiconditions){
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
                    if(Objects.equals(o.getEncounter().getEncounterType().getName(), Config.CONSULTATION.getValue())
                            && Objects.equals(o.getConcept().getName().getName(), Config.CODED_DIAGNOSIS.getValue())
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

    public Map<Encounter, List<Obs>> getPatientDocumentsForVisit(Visit visit){
        List<Obs> patientObs = encounterDao.GetAllObsForVisit(visit, Config.PATIENT_DOCUMENT.getValue(),null)
                .stream().filter(o -> !o.getConcept().getName().getName().equals(Config.DOCUMENT_TYPE.getValue()) ).collect(Collectors.toList());
        patientObs.addAll(encounterDao.GetAllObsForVisit(visit, Config.CONSULTATION.getValue(), Config.IMAGE.getValue()));
        patientObs.addAll(encounterDao.GetAllObsForVisit(visit, Config.CONSULTATION.getValue(), Config.PATIENT_VIDEO.getValue()));
        HashMap<Encounter, List<Obs>> encounterListMap = new HashMap<>();
        for (Obs obs: patientObs) {
            Encounter encounter = obs.getEncounter();
            if(!encounterListMap.containsKey(encounter))
                encounterListMap.put(encounter, new ArrayList<Obs>(){{ add(obs); }});
            else
                encounterListMap.get(encounter).add(obs);
        }
        return encounterListMap;
    }

    private org.openmrs.module.emrapi.conditionslist.Condition convertCoreConditionToEmrapiCondition(org.openmrs.Condition coreCondition) {
        org.openmrs.module.emrapi.conditionslist.Condition cListCondition = new org.openmrs.module.emrapi.conditionslist.Condition();
        Concept concept;

        if (coreCondition.getCondition().getCoded() != null) {
            concept = Context.getConceptService()
                    .getConceptByUuid(coreCondition.getCondition().getCoded().getUuid());

            if(coreCondition.getCondition().getSpecificName() == null) {
                coreCondition.getCondition().setSpecificName(coreCondition.getCondition().getCoded().getName(Context.getLocale()));
            }
        } else {
            concept = new Concept();
        }

        cListCondition.setUuid(coreCondition.getUuid());
        cListCondition.setConcept(concept);
        cListCondition.setAdditionalDetail(coreCondition.getAdditionalDetail());
        cListCondition.setPatient(coreCondition.getPatient());
        cListCondition.setConditionNonCoded(coreCondition.getCondition().getNonCoded());
        cListCondition.setOnsetDate(coreCondition.getOnsetDate());
        cListCondition.setVoided(coreCondition.getVoided());
        cListCondition.setVoidReason(coreCondition.getVoidReason());
        cListCondition.setEndDate(coreCondition.getEndDate());
        cListCondition.setCreator(coreCondition.getCreator());
        cListCondition.setDateCreated(coreCondition.getDateCreated());
        cListCondition.setStatus(convertClinicalStatus(coreCondition.getClinicalStatus()));
        if (coreCondition.getPreviousVersion() != null) {
            cListCondition.setPreviousCondition(convertCoreConditionToEmrapiCondition(coreCondition.getPreviousVersion()));
        }

        return cListCondition;
    }

    private Condition.Status convertClinicalStatus(ConditionClinicalStatus clinicalStatus) {
        Condition.Status convertedStatus = Condition.Status.ACTIVE;

        if (clinicalStatus == ConditionClinicalStatus.ACTIVE) {
            convertedStatus = Condition.Status.ACTIVE;
        } else if (clinicalStatus == ConditionClinicalStatus.INACTIVE) {
            convertedStatus = Condition.Status.INACTIVE;
        } else if (clinicalStatus == ConditionClinicalStatus.HISTORY_OF) {
            convertedStatus = Condition.Status.HISTORY_OF;
        }

        return convertedStatus;
    }
}
