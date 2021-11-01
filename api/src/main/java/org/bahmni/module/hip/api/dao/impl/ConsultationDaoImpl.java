package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.ConsultationDao;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ConsultationDaoImpl implements ConsultationDao {
    public static final String RADIOLOGY_ORDER = "Radiology Order";
    public static final String OPD = "OPD";
    private final ObsService obsService;
    private final OrderService orderService;
    public static final String CONSULTATION = "Consultation";
    public static final String CHIEF_COMPLAINT = "Chief Complaint";
    public static final String ORDER_ACTION = "DISCONTINUE";
    public static final ArrayList<String> ORDER_TYPES = new ArrayList<String>() {{
        add("Lab Order");
    }};
    private final ProgramWorkflowService programWorkflowService;
    private final EpisodeService episodeService;

    @Autowired
    public ConsultationDaoImpl(ObsService obsService, OrderService orderService, ProgramWorkflowService programWorkflowService, EpisodeService episodeService) {
        this.obsService = obsService;
        this.orderService = orderService;
        this.programWorkflowService = programWorkflowService;
        this.episodeService = episodeService;
    }

    @Override
    public List<Obs> getChiefComplaints(Patient patient, String visit, Date fromDate, Date toDate) {
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        List<Obs> chiefComplaintObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().after(fromDate)
                    && o.getEncounter().getVisit().getStartDatetime().before(toDate)
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), visit)
                    && Objects.equals(o.getConcept().getName().getName(), CHIEF_COMPLAINT)
                    && o.getValueCoded() != null
                    && o.getConcept().getName().getLocalePreferred()
            )
            {
                chiefComplaintObsMap.add(o);
            }
        }
        return chiefComplaintObsMap;
    }

    @Override
    public List<Obs> getChiefComplaintForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<Obs> obs = getAllObs(programName, fromDate, toDate, patient);
        List<Obs> obsSet = new ArrayList<>();
        for (Obs o : obs) {
            if (Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && Objects.equals(o.getConcept().getName().getName(), CHIEF_COMPLAINT)
                    && o.getValueCoded() != null
                    && o.getConcept().getName().getLocalePreferred()) {
                obsSet.add(o);
            }
        }
        return obsSet;
    }

    public List<Obs> getAllObs(String programName, Date fromDate, Date toDate, Patient patient) {
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, programWorkflowService.getProgramByName(programName), fromDate, toDate, null, null, false);
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        List<Obs> obs = new ArrayList<>();
        for (PatientProgram patientProgram : patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            Set<Encounter> encounterSet = episode.getEncounters();
            for (Encounter encounter : encounterSet) {
                obs.addAll(encounter.getAllObs());
            }
        }
        return obs;
    }

    @Override
    public List<Obs> getPhysicalExamination(Patient patient, String visit, Date fromDate, Date toDate) {
        final String[] formNames = new String[]{"Discharge Summary","Death Note", "Delivery Note", "Opioid Substitution Therapy - Intake", "Opportunistic Infection",
                "Safe Abortion", "ECG Notes", "Operative Notes", "USG Notes", "Procedure Notes", "Triage Reference", "History and Examination", "Visit Diagnoses"};
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        List<Obs> physicalExaminationObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().after(fromDate)
                    && o.getEncounter().getVisit().getStartDatetime().before(toDate)
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), visit)
                    && o.getValueCoded() == null
                    && o.getConcept().getName().getLocalePreferred()
                    && o.getObsGroup() == null
                    && !Arrays.asList(formNames).contains(o.getConcept().getName().getName())
            )
            {
                physicalExaminationObsMap.add(o);
            }
        }
        return physicalExaminationObsMap;
    }

    @Override
    public List<Order> getOrders(Patient patient, String visit, Date fromDate, Date toDate) {
        if(Objects.equals(visit, OPD)) { ORDER_TYPES.add(RADIOLOGY_ORDER); }
        List<Order> orders = orderService.getAllOrdersByPatient(patient);
        return orders.stream().filter(order -> matchesVisitType(visit, order))
                              .filter(order -> order.getEncounter().getVisit().getStartDatetime().after(fromDate))
                              .filter(order -> order.getEncounter().getVisit().getStartDatetime().before(toDate))
                              .filter(order -> order.getDateStopped() == null && !Objects.equals(order.getAction().toString(), ORDER_ACTION))
                              .filter(order -> ORDER_TYPES.contains(order.getOrderType().getName()))
                              .collect(Collectors.toList());
    }

    private boolean matchesVisitType(String visitType, Order order) {
        return order.getEncounter().getVisit().getVisitType().getName().equals(visitType);
    }

    @Override
    public List<Order> getOrdersForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<Order> orderSet = new ArrayList<>();
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, programWorkflowService.getProgramByName(programName), fromDate, toDate, null, null, false);
        Set<PatientProgram> patientProgramSet = new HashSet<>(patientPrograms);
        for (PatientProgram patientProgram : patientProgramSet) {
            Episode episode = episodeService.getEpisodeForPatientProgram(patientProgram);
            Set<Encounter> encounterSet = episode.getEncounters();
            for (Encounter encounter : encounterSet) {
                for (Order order : encounter.getOrders()) {
                    if (order.getDateStopped() == null && !Objects.equals(order.getAction().toString(), ORDER_ACTION) && ORDER_TYPES.contains(order.getOrderType().getName())) {
                        orderSet.add(order);
                    }
                }
            }
        }
        return orderSet;
    }

    @Override
    public List<Obs> getPhysicalExaminationForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        final String[] formNames = new String[]{"Discharge Summary", "Death Note", "Delivery Note", "Opioid Substitution Therapy - Intake", "Opportunistic Infection",
                "Safe Abortion", "ECG Notes", "Operative Notes", "USG Notes", "Procedure Notes", "Triage Reference", "History and Examination", "Visit Diagnoses"};
        List<Obs> physicalExaminationObsMap = new ArrayList<>();
        List<Obs> obs = getAllObs(programName, fromDate, toDate, patient);
        for (Obs o : obs) {
            if (Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getValueCoded() == null
                    && o.getConcept().getName().getLocalePreferred()
                    && o.getObsGroup() == null
                    && !Arrays.asList(formNames).contains(o.getConcept().getName().getName())) {
                physicalExaminationObsMap.add(o);
            }
        }
        return physicalExaminationObsMap;
    }
}

