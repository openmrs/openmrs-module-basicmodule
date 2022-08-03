package org.bahmni.module.hip.api.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.hip.api.dao.DiagnosticReportDao;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;


@Repository
public class DiagnosticReportDaoImpl implements DiagnosticReportDao {

    private SessionFactory sessionFactory;
    private PersonService personService;
    private ObsService obsService;
    private ConceptService conceptService;
    private EncounterService encounterService;
    private EncounterDao encounterDao;
    private PatientService patientService;

    private static Logger logger = LogManager.getLogger(DiagnosticReportDaoImpl.class);
    private static final String LAB_REPORT = "LAB_REPORT";
    private static final String LAB_RESULT = "LAB_RESULT";


    @Autowired
    public DiagnosticReportDaoImpl(PersonService personService, ObsService obsService,
                                   ConceptService conceptService, EncounterService encounterService,
                                   EncounterDao encounterDao, SessionFactory sessionFactory, PatientService patientService)
    {
        this.obsService = obsService;
        this.personService = personService;
        this.conceptService = conceptService;
        this.encounterService = encounterService;
        this.encounterDao = encounterDao;
        this.sessionFactory = sessionFactory;
        this.patientService = patientService;
    }


    private List<Obs> getAllObsForDiagnosticReports(String patientUUID) {
        Person person = personService.getPersonByUuid(patientUUID);
        Concept concept = conceptService.getConcept("LAB_REPORT");
        List<Obs> obs = obsService.getObservationsByPersonAndConcept(person,concept).stream().filter(o -> o.getOrder() == null).collect(Collectors.toList());
        return obs;
    }

    @Override
    public String getTestNameForLabReports(Obs obs){
        Query query = this.sessionFactory.getCurrentSession().createSQLQuery("select concept_id from fhir_diagnostic_report where date_created = :obsDateTime ;");
        query.setParameter("obsDateTime", obs.getDateCreated());

        return query.list().size() != 0 ? conceptService.getConcept(query.list().get(0).hashCode()).getName().getName() : null;
    }

    @Override
    public Map<Encounter,List<Obs>> getAllUnorderedUploadsForVisit(String patientUUID, Visit visit){
        Map<Encounter,List<Obs>> labReportsMap = new HashMap<>();;
        List<Obs> labReports = getAllObsForDiagnosticReports(patientUUID);
        List<Encounter> encounters = encounterService.getEncountersByVisit(visit,false);
        List<Encounter> nextEncounters = encounterService.getEncountersByPatient(patientService.getPatientByUuid(patientUUID)).stream().filter(e ->
                encounters.get(encounters.size()-1).getId() < e.getId()
        ).collect(Collectors.toList());
        for (Obs obs: labReports) {
            for (Encounter encounter: encounters) {
                Encounter nextEncounter;
                Date nextEncounterDate = nextEncounters.size() != 0 ? nextEncounters.get(0).getDateCreated() : new Date();
                if(encounters.indexOf(encounter) < (encounters.size() - 1)){
                    nextEncounter = encounterService.getEncounter(encounters.get(encounters.indexOf(encounter)+1).getId());
                    nextEncounterDate = nextEncounter.getDateCreated();
                }
                if(obs.getDateCreated().equals(encounter.getDateCreated()) || (obs.getDateCreated().before(nextEncounterDate) && obs.getDateCreated().after(encounter.getDateCreated()))){
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
        return labReportsMap;
    }

    @Override
    public Map<Order,Obs> getAllOrderedTestUploads(List<Visit> visitList) {
        Map<Order,Obs> documentObs = new HashMap<>();
        for (Visit visit : visitList) {
            for (Obs o : encounterDao.GetAllObsForVisit(visit, LAB_RESULT, LAB_REPORT).stream().filter(o -> !o.getVoided()).collect(Collectors.toList())) {
                documentObs.put(o.getOrder(), o);
            }
        }
        return documentObs;
    }
}