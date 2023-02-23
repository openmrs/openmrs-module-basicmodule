package org.bahmni.module.hip.web.builders;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;

public class DrugOrderBuilder {
    private String drugOrderUUID;
    private Encounter encounter;

    public DrugOrderBuilder(String drugOrderUUID) {
        this.drugOrderUUID = drugOrderUUID;
    }

    public DrugOrderBuilder withEncounter(Encounter encounter) {
        this.encounter = encounter;
        return this;
    }

    public static DrugOrderBuilder getBuilder(String drugOrderUUID) {
        return new DrugOrderBuilder(drugOrderUUID);
    }

    public DrugOrder build() {
        DrugOrder drugOrder = new DrugOrder();
        drugOrder.setUuid(drugOrderUUID);
        drugOrder.setEncounter(this.encounter);

        return drugOrder;
    }
}
