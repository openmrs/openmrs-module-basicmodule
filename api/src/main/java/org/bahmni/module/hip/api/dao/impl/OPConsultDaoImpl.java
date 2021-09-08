package org.bahmni.module.hip.api.dao.impl;
import org.bahmni.module.hip.api.dao.OPConsultDao;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.emrapi.conditionslist.ConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.openmrs.api.OrderService;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class OPConsultDaoImpl implements OPConsultDao {
    public static final String CHIEF_COMPLAINT = "Chief Complaint";
    public static final String PROCEDURE_NOTES = "Procedure Notes, Procedure";
    public static final String CONSULTATION = "Consultation";
    private static final String CODED_DIAGNOSIS = "Coded Diagnosis";
    public static final String OPD = "OPD";
    public static final String ORDER_ACTION = "DISCONTINUE";
    public static final String LAB_ORDER = "Lab Order";
    public static final String RADIOLOGY_ORDER = "Radiology Order";
    private final ObsService obsService;
    private final ConditionService conditionService;
    private final EncounterService encounterService;
    private final OrderService orderService;


    @Autowired
    public OPConsultDaoImpl(ObsService obsService, ConditionService conditionService, EncounterService encounterService, OrderService orderService) {
        this.obsService = obsService;
        this.conditionService = conditionService;
        this.encounterService = encounterService;
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
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), OPD)
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
    public Map<Encounter, List<Condition>> getMedicalHistoryConditions(Patient patient, String visit, Date fromDate, Date toDate) {
        final String conditionStatusHistoryOf = "HISTORY_OF";
        final String conditionStatusActive = "ACTIVE";
        List<Encounter> encounters = encounterService.getEncountersByPatient(patient)
                                                     .stream()
                                                     .filter(encounter -> Objects.equals(encounter.getEncounterType().getName(), "Consultation") &&
                                                                         encounter.getDateCreated().after(fromDate) &&
                                                                         encounter.getDateCreated().before(toDate) &&
                                                                         Objects.equals(encounter.getVisit().getVisitType().getName(), OPD))
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
    public List<Obs> getPhysicalExamination(Patient patient, String visit, Date fromDate, Date toDate) {
        final String[] formNames = new String[]{"Discharge Summary","Death Note", "Delivery Note", "Opioid Substitution Therapy - Intake", "Opportunistic Infection",
                "Safe Abortion", "ECG Notes", "Operative Notes", "USG Notes", "Procedure Notes", "Triage Reference", "History and Examination", "Visit Diagnoses"};
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        List<Obs> physicalExaminationObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().after(fromDate)
                    && o.getEncounter().getVisit().getStartDatetime().before(toDate)
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), OPD)
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
    public List<Obs> getProcedures(Patient patient, String visit, Date fromDate, Date toDate) {
        List<Obs> patientObs = obsService.getObservationsByPerson(patient);
        List<Obs> proceduresObsMap = new ArrayList<>();
        for(Obs o :patientObs){
            if(Objects.equals(o.getEncounter().getEncounterType().getName(), CONSULTATION)
                    && o.getEncounter().getVisit().getStartDatetime().after(fromDate)
                    && o.getEncounter().getVisit().getStartDatetime().before(toDate)
                    && Objects.equals(o.getEncounter().getVisit().getVisitType().getName(), OPD)
                    && Objects.equals(o.getConcept().getName().getName(), PROCEDURE_NOTES)
                    && !o.getVoided()
            )
            {
                proceduresObsMap.add(o);
            }
        }
        return proceduresObsMap;
    }

    private boolean matchesVisitType(String visitType, Order order) {
        return order.getEncounter().getVisit().getVisitType().getName().equals(visitType);
    }

    @Override
    public List<Order> getOrders(Patient patient, String visit, Date fromDate, Date toDate) {
        List<Order> orders = orderService.getAllOrdersByPatient(patient);
        List<Order> orderMap = orders.stream().filter(order -> matchesVisitType(visit, order))
                .filter(order -> order.getEncounter().getVisit().getStartDatetime().after(fromDate))
                .filter(order -> order.getEncounter().getVisit().getStartDatetime().before(toDate))
                .filter(order -> order.getDateStopped() == null && order.getAction().toString()!= ORDER_ACTION)
                .filter(order -> order.getOrderType().getName().equals(LAB_ORDER) || order.getOrderType().getName().equals(RADIOLOGY_ORDER))
                .collect(Collectors.toList());
        return orderMap;
    }
}
