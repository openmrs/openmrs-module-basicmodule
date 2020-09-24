package org.bahmni.module.hip.web.service;


import org.apache.log4j.Logger;
import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.OpenMrsPrescription;
import org.openmrs.DrugOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {
    private static final Logger log = Logger.getLogger(PrescriptionService.class);

    private final OpenMRSDrugOrderClient openMRSDrugOrderClient;
    private final FhirBundledPrescriptionBuilder fhirBundledPrescriptionBuilder;

    @Autowired
    public PrescriptionService(OpenMRSDrugOrderClient openMRSDrugOrderClient, FhirBundledPrescriptionBuilder fhirBundledPrescriptionBuilder) {
        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
        this.fhirBundledPrescriptionBuilder = fhirBundledPrescriptionBuilder;
    }


    public List<BundledPrescriptionResponse> getPrescriptions(String patientIdUuid, Date fromDate, Date toDate) {
        List<DrugOrder> drugOrders = openMRSDrugOrderClient.getDrugOrdersByDateFor(
                patientIdUuid,
                fromDate,
                toDate);

        if (CollectionUtils.isEmpty(drugOrders))
            return new ArrayList<>();

        return bundlePrescriptionsFor(drugOrders);
    }

    private List<BundledPrescriptionResponse> bundlePrescriptionsFor(List<DrugOrder> drugOrders) {

        List<OpenMrsPrescription> openMrsPrescriptions = buildOpenMRSPrescriptionsFor(drugOrders);

        return openMrsPrescriptions
                .stream()
                .map(fhirBundledPrescriptionBuilder::buildFor)
                .collect(Collectors.toList());
    }

    private List<OpenMrsPrescription> buildOpenMRSPrescriptionsFor(List<DrugOrder> drugOrders) {
        return drugOrders
                .stream()
                .collect(Collectors.groupingBy(order -> order.getEncounter().getUuid()))
                .values()
                .stream()
                .map(OpenMrsPrescription::new)
                .collect(Collectors.toList());
    }

}
