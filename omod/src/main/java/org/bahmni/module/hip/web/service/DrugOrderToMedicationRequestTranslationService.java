package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.DrugOrder;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class DrugOrderToMedicationRequestTranslationService {

    private MedicationRequestTranslator medicationRequestTranslator;

    @Autowired
    public DrugOrderToMedicationRequestTranslationService(MedicationRequestTranslator medicationRequestTranslator) {
        this.medicationRequestTranslator = medicationRequestTranslator;
    }

    MedicationRequest toMedicationRequest(DrugOrder drugOrder) {

        return medicationRequestTranslator.toFhirResource(drugOrder);
    }
}
