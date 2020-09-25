package org.bahmni.module.hip.web.service;


import org.apache.log4j.Logger;
import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.DrugOrders;
import org.bahmni.module.hip.web.model.OpenMrsPrescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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


    public List<BundledPrescriptionResponse> getPrescriptions(String patientIdUuid, DateRange dateRange) {
        DrugOrders drugOrders = new DrugOrders(openMRSDrugOrderClient.getDrugOrdersByDateFor(patientIdUuid, dateRange));

        if (drugOrders.isEmpty())
            return new ArrayList<>();

        List<OpenMrsPrescription> openMrsPrescriptions = OpenMrsPrescription
                .from(drugOrders.groupByEncounter());

        return openMrsPrescriptions
                .stream()
                .map(fhirBundledPrescriptionBuilder::fhirBundleResponseFor)
                .collect(Collectors.toList());
    }
}
