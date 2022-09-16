package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.api.dao.DiagnosticReportDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Repository
public class DiagnosticReportDaoImpl implements DiagnosticReportDao {

    private SessionFactory sessionFactory;
    private PersonService personService;
    private ObsService obsService;
    private ConceptService conceptService;
    private EncounterService encounterService;
    private PatientService patientService;


    @Autowired
    public DiagnosticReportDaoImpl(PersonService personService, ObsService obsService,
                                   ConceptService conceptService, EncounterService encounterService,
                                   SessionFactory sessionFactory, PatientService patientService)
    {
        this.obsService = obsService;
        this.personService = personService;
        this.conceptService = conceptService;
        this.encounterService = encounterService;
        this.sessionFactory = sessionFactory;
        this.patientService = patientService;
    }


    private List<Obs> getAllObsForDiagnosticReports(String patientUUID, Boolean linkedWithOrder) {
        Person person = personService.getPersonByUuid(patientUUID);
        Concept concept = conceptService.getConcept(Config.LAB_REPORT.getValue());
        List<Obs> obs = obsService.getObservationsByPersonAndConcept(person,concept);
        if(linkedWithOrder)
           return obs.stream().filter(o -> o.getOrder() != null).collect(Collectors.toList());
        return obs.stream().filter(o -> o.getOrder() == null).collect(Collectors.toList());
    }

    @Override
    public Map<Encounter,List<Obs>> getAllUnorderedUploadsForVisit(String patientUUID, Visit visit){
        Map<Encounter,List<Obs>> labReportsMap = new HashMap<>();;
        List<Obs> labReports = getAllObsForDiagnosticReports(patientUUID,false);
        List<Encounter> encounters = encounterService.getEncountersByVisit(visit,false);
        if(encounters.size() != 0) {
            List<Encounter> nextEncounters = encounterService.getEncountersByPatient(patientService.getPatientByUuid(patientUUID)).stream().filter(e ->
                    encounters.get(encounters.size() - 1).getId() < e.getId()
            ).collect(Collectors.toList());
            for (Obs obs : labReports) {
                for (Encounter encounter : encounters) {
                    Encounter nextEncounter;
                    Date nextEncounterDate = nextEncounters.size() != 0 ? nextEncounters.get(0).getDateCreated() : new Date();
                    if (encounters.indexOf(encounter) < (encounters.size() - 1)) {
                        nextEncounter = encounterService.getEncounter(encounters.get(encounters.indexOf(encounter) + 1).getId());
                        nextEncounterDate = nextEncounter.getDateCreated();
                    }
                    if (obs.getDateCreated().equals(encounter.getDateCreated()) || (obs.getDateCreated().before(nextEncounterDate) && obs.getDateCreated().after(encounter.getDateCreated()))) {
                        if (labReportsMap.containsKey(encounter)) {
                            labReportsMap.get(encounter).add(obs);
                        } else {
                            labReportsMap.put(encounter, new ArrayList<Obs>() {{
                                add(obs);
                            }});
                        }
                    }
                }
            }
        }
        return labReportsMap;
    }

    @Override
    public Map<Encounter,List<Obs>> getAllOrderedTestUploads(String patientUuid,Visit visit) {
        Map<Encounter,List<Obs>> documentObs = new HashMap<>();
        List<Obs> obsList = getAllObsForDiagnosticReports(patientUuid,true);
        List<Encounter> encounters = encounterService.getEncountersByVisit(visit,false);

        for (Obs obs : obsList) {
            Encounter orderEncounter = obs.getOrder().getEncounter();
            if(encounters.contains(orderEncounter)) {
                if (documentObs.containsKey(orderEncounter)) {
                    documentObs.get(orderEncounter).add(obs);
                } else {
                    documentObs.put(orderEncounter, new ArrayList<Obs>() {{
                        add(obs);
                    }});
                }
            }
        }
        return documentObs;
    }
}