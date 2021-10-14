package org.bahmni.module.hip.api.dao.impl;

import org.bahmni.module.hip.api.dao.ConsultationDao;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Arrays;
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
    public static final ArrayList<String> ORDER_TYPES = new ArrayList<String>() {{ add("Lab Order"); }};

    @Autowired
    public ConsultationDaoImpl(ObsService obsService, OrderService orderService) {
        this.obsService = obsService;
        this.orderService = orderService;
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
}
