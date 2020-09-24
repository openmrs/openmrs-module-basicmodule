package org.bahmni.module.hip.web.service;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Patient;

import java.util.List;
import java.util.Set;

@Getter
class PrescriptionGenerationRequest {
    private Encounter encounter;
    private Set<EncounterProvider> encounterProviders;
    private List<DrugOrder> drugOrders;
    private Patient patient;

    PrescriptionGenerationRequest(@NotEmpty List<DrugOrder> drugOrders) {
        this.encounter = drugOrders.get(0).getEncounter();
        this.encounterProviders = this.encounter.getEncounterProviders();
        this.patient = this.encounter.getPatient();
        this.drugOrders = drugOrders;
    }
}
