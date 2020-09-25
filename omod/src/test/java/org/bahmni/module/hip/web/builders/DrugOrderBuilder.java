package org.bahmni.module.hip.web.builders;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;

public class DrugOrderBuilder {
    private Encounter encounter;

    public DrugOrderBuilder withEncounterUUID(String encounterUUID) {
        Encounter encounter = new Encounter();
        encounter.setUuid(encounterUUID);
        this.encounter = encounter;
        return this;
    }

    public static DrugOrderBuilder getBuilder() {
        return new DrugOrderBuilder();
    }

    public DrugOrder build() {
        DrugOrder drugOrder = new DrugOrder();
        drugOrder.setEncounter(this.encounter);
        return drugOrder;
    }
}
