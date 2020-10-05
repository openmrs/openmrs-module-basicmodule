package org.bahmni.module.hip.web.builders;

import lombok.AllArgsConstructor;
import org.openmrs.Encounter;

@AllArgsConstructor
public class EncounterBuilder {
    private String encounterUUID;

    public static EncounterBuilder getBuilder(String encounterUUID) {
        return new EncounterBuilder(encounterUUID);
    }

    public Encounter build() {
        Encounter encounter = new Encounter();
        encounter.setUuid(encounterUUID);

        return encounter;
    }

}
