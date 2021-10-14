package org.bahmni.module.hip.api.dao.impl;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.emrapi.conditionslist.ConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.Objects;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Repository
public class OPConsultDaoImpl implements OPConsultDao {
    public static final String PROCEDURE_NOTES = "Procedure Notes, Procedure";
    public static final String CONSULTATION = "Consultation";
    private static final String CODED_DIAGNOSIS = "Coded Diagnosis";

    private final ObsService obsService;
    private final ConditionService conditionService;
    private final EncounterService encounterService;


    @Autowired
    public OPConsultDaoImpl(ObsService obsService, ConditionService conditionService, EncounterService encounterService) {
        this.obsService = obsService;
        this.conditionService = conditionService;
        this.encounterService = encounterService;
    }

    @Override
    public Map<Encounter, List<Condition>> getMedicalHistoryConditions(Patient patient, String visit, Date fromDate, Date toDate) {
        final String conditionStatusHistoryOf = "HISTORY_OF";
        final String conditionStatusActive = "ACTIVE";
        List<Encounter> encounters = encounterService.getEncountersByPatient(patient)
                                                     .stream()
                                                     .filter(encounter -> Objects.equals(encounter.getEncounterType().getName(), "Consultation") &&
                                                                         encounter.getDateCreated().after(fromDate) &&
                                                                         encounter.getDateCreated().before(toDate) &&
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
                Date nextEncounterDate = new Date();
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

    @Override
    public List<Obs> getMedicalHistoryDiagnosis(Patient patient, String visit, Date fromDate, Date toDate) {
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        List<Obs> medicalHistoryDiagnosisObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().after(fromDate)
                    && o.getEncounter().getVisit().getStartDatetime().before(toDate)
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
    public List<Obs> getProcedures(Patient patient, String visit, Date fromDate, Date toDate) {
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        List<Obs> proceduresObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().after(fromDate)
                    && o.getEncounter().getVisit().getStartDatetime().before(toDate)
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
}
