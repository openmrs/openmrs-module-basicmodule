package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicationRequestService {

    private OpenMRSDrugOrderClient openMRSDrugOrderClient;
    private MedicationRequestTranslator medicationRequestTranslator;

    @Autowired
    public MedicationRequestService(OpenMRSDrugOrderClient openMRSDrugOrderClient,
                                    MedicationRequestTranslator medicationRequestTranslator) {

        this.openMRSDrugOrderClient = openMRSDrugOrderClient;
        this.medicationRequestTranslator = medicationRequestTranslator;
    }

    List<MedicationRequest> medicationRequestFor(String patientId, String byVisitType) {

        List<DrugOrder> drugOrders = openMRSDrugOrderClient.drugOrdersFor(patientId, byVisitType);

        return translateToMedicationRequest(drugOrders);
    }

    private List<MedicationRequest> translateToMedicationRequest(List<DrugOrder> drugOrders) {
        return drugOrders
                .stream()
                .map(medicationRequestTranslator::toFhirResource)
                .collect(Collectors.toList());
    }
}