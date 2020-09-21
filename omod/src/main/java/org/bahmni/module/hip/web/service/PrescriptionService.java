package org.bahmni.module.hip.web.service;


import org.apache.log4j.Logger;
import org.bahmni.module.hip.api.dao.PrescriptionOrderDao;
import org.bahmni.module.hip.web.controller.HipContext;
import org.bahmni.module.hip.web.model.Prescription;
import org.hl7.fhir.r4.model.Organization;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
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

    public List<Prescription> getPrescriptions(String patientIdUuid, Date fromDate, Date toDate) {
        PrescriptionOrderDao prescriptionDao = getPrescriptionDao();
        Patient patient = Context.getPatientService().getPatientByUuid(patientIdUuid);
        OrderType drugOrderType = Context.getOrderService().getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        List<DrugOrder> drugOrders = prescriptionDao.getDrugOrders(patient, fromDate, toDate, drugOrderType);
        return mapToPrescriptions(drugOrders);
    }

    public String getEncounterUuidForOrder(DrugOrder order) {
        return order.getEncounter().getUuid();
    }

    private List<Prescription> mapToPrescriptions(List<DrugOrder> drugOrders) {
        if (drugOrders.isEmpty()) {
            return new ArrayList<>();
        }

//        Map<String, List<DrugOrder>> prescriptionsMap = new HashMap<>();
//        for (DrugOrder drugOrder : drugOrders) {
//            String encounterUuid = drugOrder.getEncounter().getUuid();
//            List<DrugOrder> drugOrdersInPrescription = prescriptionsMap.get(encounterUuid);
//            if (drugOrdersInPrescription != null) {
//                drugOrdersInPrescription.add(drugOrder);
//            } else {
//                ArrayList<DrugOrder> prescriptionOrders = new ArrayList<>();
//                prescriptionOrders.add(drugOrder);
//                prescriptionsMap.put(encounterUuid, prescriptionOrders);
//            }
//        }

        Map<String, List<DrugOrder>> prescriptionsMap = Optional.ofNullable(drugOrders)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.groupingBy(this::getEncounterUuidForOrder));

        List<Prescription> prescriptions = new ArrayList<>();
        prescriptionsMap.forEach((key,value) -> {
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

    private PrescriptionOrderDao getPrescriptionDao() {
        List<PrescriptionOrderDao> registeredComponents = Context.getRegisteredComponents(PrescriptionOrderDao.class);
        if (registeredComponents != null & !registeredComponents.isEmpty()) {
            return registeredComponents.get(0);
        }
        return null;
    }


    private Organization getOrganization(Encounter encounter) {
        AdministrationService administrationService = Context.getAdministrationService();
        String hfrId = administrationService.getGlobalProperty(Constants.PROP_HFR_ID);
        String hfrName = administrationService.getGlobalProperty(Constants.PROP_HFR_NAME);
        String hfrSystem = administrationService.getGlobalProperty(Constants.PROP_HFR_SYSTEM);
        return FHIRUtils.createOrgInstance(hfrId, hfrName, hfrSystem);
    }
}
