package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;
import org.openmrs.DrugOrder;

import java.util.ArrayList;

class DrugOrderToMedicationRequestTranslationService {

    static MedicationRequest toMedicationRequest(DrugOrder drugOrder) {

        // MedicationRequestTranslator medicationRequestTranslator = new MedicationRequestTranslatorImpl();
        //return medicationRequestTranslator.toFhirResource(drugOrder);

        MedicationRequest.MedicationRequestDispenseRequestComponent medicationRequestDispenseRequestComponent =
                new MedicationRequest.MedicationRequestDispenseRequestComponent();

        medicationRequestDispenseRequestComponent.setQuantity(new Quantity(drugOrder.getQuantity()));


        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setDispenseRequest(medicationRequestDispenseRequestComponent);

        Dosage dosage = new Dosage();
        Dosage.DosageDoseAndRateComponent dosageDoseAndRateComponent = new Dosage.DosageDoseAndRateComponent();
        dosageDoseAndRateComponent.setDose(new Quantity(drugOrder.getDose()));
        ArrayList<Dosage.DosageDoseAndRateComponent> dosageDoseAndRateComponents = new ArrayList<>();
        dosageDoseAndRateComponents.add(dosageDoseAndRateComponent);
        dosage.setDoseAndRate(dosageDoseAndRateComponents);

        ArrayList<Dosage> dosages = new ArrayList<>();

        dosages.add(dosage);

        medicationRequest.setDosageInstruction(dosages);

        return medicationRequest;
    }
}
