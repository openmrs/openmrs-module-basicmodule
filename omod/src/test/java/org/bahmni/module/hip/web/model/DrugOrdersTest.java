package org.bahmni.module.hip.web.model;

import org.bahmni.module.hip.web.builders.DrugOrderBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DrugOrdersTest {
    @Test
    public void shouldGroupDrugOrdersByTheirEncounterUUIDs() {
        String UUIDOne = "0a90531a-285c-438b-b265-bb3abb4745bd";
        String UUIDTwo = "0b90531a-285c-238b-b485-bb3abb4745bd";
        DrugOrder drugOrderOne = DrugOrderBuilder.getBuilder()
                .withEncounterUUID(UUIDOne)
                .build();
        DrugOrder drugOrderTwo = DrugOrderBuilder.getBuilder()
                .withEncounterUUID(UUIDTwo)
                .build();

        List<DrugOrder> openMrsDrugOrders = new ArrayList<DrugOrder>() {{
            add(drugOrderOne);
            add(drugOrderTwo);
        }};

        DrugOrders drugOrders = new DrugOrders(openMrsDrugOrders);

        Map<Encounter, DrugOrders> encounterDrugOrdersMap = drugOrders.groupByEncounter();

        List<String> actualEncounterUUIDs = encounterDrugOrdersMap
                .keySet().stream()
                .map(Encounter::getUuid)
                .collect(Collectors.toList());

        ArrayList<String> expectedEncounterUUIDs = new ArrayList<String>() {{
            add(UUIDOne);
            add(UUIDTwo);
        }};

        Assert.assertEquals(expectedEncounterUUIDs, actualEncounterUUIDs);
    }
}