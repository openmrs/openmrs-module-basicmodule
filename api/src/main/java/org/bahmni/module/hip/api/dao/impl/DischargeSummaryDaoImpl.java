package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.DischargeSummaryDao;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class DischargeSummaryDaoImpl implements DischargeSummaryDao {

    public static final String CONSULTATION = "Consultation";
    public static final String PROCEDURE_NOTES = "Procedure Notes";
    private final ObsService obsService;

    @Autowired
    public DischargeSummaryDaoImpl(ObsService obsService) {
        this.obsService = obsService;
    }

    private boolean matchesVisitType(String visitType, Obs obs) {
        return obs.getEncounter().getVisit().getVisitType().getName().equals(visitType);
    }

    @Override
    public List<Obs> getCarePlan(Patient patient, String visit, Date fromDate, Date toDate) {
        final String obsName = "Discharge Summary";
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);

        List<Obs> carePlanObs = patientObs.stream().filter(obs -> matchesVisitType(visit, obs))
                .filter(obs -> obs.getEncounter().getVisit().getStartDatetime().after(fromDate))
                .filter(obs -> obs.getEncounter().getVisit().getStartDatetime().before(toDate))
                .filter(obs -> obsName.equals(obs.getConcept().getName().getName()))
                .filter(obs -> obs.getConcept().getName().getLocalePreferred())
                .collect(Collectors.toList());

        return carePlanObs;
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
                    && o.getObsGroup() == null
                    && Objects.equals(o.getConcept().getName().getName(), PROCEDURE_NOTES)
            )
            {
                proceduresObsMap.add(o);
            }
        }
        return proceduresObsMap;
    }
}