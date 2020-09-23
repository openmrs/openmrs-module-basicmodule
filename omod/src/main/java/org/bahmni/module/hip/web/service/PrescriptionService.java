package org.bahmni.module.hip.web.service;


import org.apache.log4j.Logger;
import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.bahmni.module.hip.web.model.Prescription;
import org.hl7.fhir.r4.model.Organization;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private static final Logger log = Logger.getLogger(PrescriptionService.class);

    private final OrderService orderService;
    private final PrescriptionOrderDao prescriptionOrderDao;
    private final PatientService patientService;

    @Autowired
    public PrescriptionService(OrderService orderService,
                               PrescriptionOrderDao prescriptionOrderDao,
                               PatientService patientService) {
        this.orderService = orderService;
        this.prescriptionOrderDao = prescriptionOrderDao;
        this.patientService = patientService;
    }


    public List<Prescription> getPrescriptions(String patientIdUuid, Date fromDate, Date toDate) {
        Patient patient = patientService.getPatientByUuid(patientIdUuid);
        OrderType drugOrderType = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        List<DrugOrder> drugOrders = prescriptionOrderDao.getDrugOrders(patient, fromDate, toDate, drugOrderType);
        return mapToPrescriptions(drugOrders);
    }

    public String getEncounterUuidForOrder(DrugOrder order) {
        return order.getEncounter().getUuid();
    }

    private List<Prescription> mapToPrescriptions(List<DrugOrder> drugOrders) {
        if (drugOrders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<DrugOrder>> drugOrderMap = Optional.ofNullable(drugOrders)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.groupingBy(this::getEncounterUuidForOrder));

        List<Prescription> prescriptions = new ArrayList<>();
        drugOrderMap.forEach((key,value) -> {
            try {
                prescriptions.add(createPrescription(value));
            } catch (Exception e) {
                log.error("Error occurred while trying to create Prescription", e);
            }
        });

        return prescriptions;
    }

    private Prescription createPrescription(List<DrugOrder> drugOrders) throws Exception {
        Encounter encounter = drugOrders.get(0).getEncounter();
        return new PrescriptionGenerator(getOrgContext(encounter), encounter, drugOrders).generate();
    }

    private OrgContext getOrgContext(Encounter encounter) {
        Organization organization = getOrganization(encounter);
        return OrgContext.builder()
                .organization(organization)
                .webUrl(getWebUrl())
                .build();
    }

    private String getWebUrl() {
        AdministrationService administrationService = Context.getAdministrationService();
        return administrationService.getGlobalProperty(Constants.PROP_HFR_URL);
    }

    private Organization getOrganization(Encounter encounter) {
        AdministrationService administrationService = Context.getAdministrationService();
        String hfrId = administrationService.getGlobalProperty(Constants.PROP_HFR_ID);
        String hfrName = administrationService.getGlobalProperty(Constants.PROP_HFR_NAME);
        String hfrSystem = administrationService.getGlobalProperty(Constants.PROP_HFR_SYSTEM);
        return FHIRUtils.createOrgInstance(hfrId, hfrName, hfrSystem);
    }
}
