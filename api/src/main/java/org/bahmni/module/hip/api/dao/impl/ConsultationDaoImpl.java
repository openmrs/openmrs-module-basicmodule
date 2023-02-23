package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.api.dao.ConsultationDao;
import org.bahmni.module.hip.api.dao.EncounterDao;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
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

import static org.bahmni.module.hip.api.dao.Constants.ORDER_ACTION;

@Repository
public class ConsultationDaoImpl implements ConsultationDao {

    public static final ArrayList<String> ORDER_TYPES = new ArrayList<String>() {{
        add(Config.LAB_ORDER.getValue());
        add(Config.RADIOLOGY_ORDER.getValue());
    }};
    private final ProgramWorkflowService programWorkflowService;
    private final EpisodeService episodeService;
    private final EncounterDao encounterDao;
    private final ObsService obsService;
    private final OrderService orderService;

    @Autowired
    public ConsultationDaoImpl(ObsService obsService, OrderService orderService, ProgramWorkflowService programWorkflowService, EpisodeService episodeService, EncounterDao encounterDao) {
        this.obsService = obsService;
        this.orderService = orderService;
        this.programWorkflowService = programWorkflowService;
        this.episodeService = episodeService;
        this.encounterDao = encounterDao;
    }

    @Override
    public List<Obs> getChiefComplaints(Visit visit) {
        List<Obs> chiefComplaintObsMap = encounterDao.GetAllObsForVisit(visit,Config.CONSULTATION.getValue(),Config.CHIEF_COMPLAINT.getValue())
                .stream().filter(o -> o.getValueCoded() != null && o.getConcept().getName().getLocalePreferred())
                .collect(Collectors.toList());
        return chiefComplaintObsMap;
    }

    @Override
    public List<Obs> getChiefComplaintForProgram(String programName, Date fromDate, Date toDate, Patient patient) {
        List<Obs> obs = getAllObs(programName, fromDate, toDate, patient);
        List<Obs> obsSet = new ArrayList<>();
        for (Obs o : obs) {
            if (Objects.equals(o.getEncounter().getEncounterType().getName(), Config.CONSULTATION.getValue())
                    && Objects.equals(o.getConcept().getName().getName(), Config.CHIEF_COMPLAINT.getValue())
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
    public List<Obs> getPhysicalExamination(Visit visit) {
        final String[] formNames = Config.Forms_To_Ignore_In_Physical_Examination.getValue().split("\\s*,\\s*");
        List<Obs> physicalExaminationObsMap = encounterDao.GetAllObsForVisit(visit,Config.CONSULTATION.getValue(),null)
                .stream().filter(o -> o.getValueCoded() == null
                        &&  o.getObsGroup() == null
                        && !Arrays.asList(formNames).contains(o.getConcept().getName().getName()) )
                .collect(Collectors.toList());
        return physicalExaminationObsMap;
    }

    @Override
    public List<Order> getOrders(Visit visit) {
        return  encounterDao.GetOrdersForVisit(visit).stream()
                .filter(order -> order.getDateStopped() == null && !Objects.equals(order.getAction().toString(), ORDER_ACTION))
                .filter(order -> ORDER_TYPES.contains(order.getOrderType().getName()))
                .collect(Collectors.toList());
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
        final String[] formNames = Config.Forms_To_Ignore_In_Physical_Examination.getValue().split("\\s*,\\s*");
        List<Obs> physicalExaminationObsMap = new ArrayList<>();
        List<Obs> obs = getAllObs(programName, fromDate, toDate, patient);
        for (Obs o : obs) {
            if (Objects.equals(o.getEncounter().getEncounterType().getName(), Config.CONSULTATION.getValue())
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

