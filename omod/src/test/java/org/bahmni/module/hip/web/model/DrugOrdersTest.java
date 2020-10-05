package org.bahmni.module.hip.web.model;

import org.bahmni.module.hip.web.builders.DrugOrderBuilder;
import org.bahmni.module.hip.web.builders.EncounterBuilder;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DrugOrdersTest {
    @Test
    public void shouldGroupDrugOrdersByTheirEncounterUUIDs() {
        String encounterUUIDOne = "0a90531a-285c-438b-b265-bb3abb4745bd";
        String encounterUUIDTwo = "0b90531a-285c-238b-b485-bb3abb4745bd";

        Encounter encounterOne = EncounterBuilder.getBuilder(encounterUUIDOne).build();
        Encounter encounterTwo = EncounterBuilder.getBuilder(encounterUUIDTwo).build();

        DrugOrder drugOrderOne = DrugOrderBuilder.getBuilder("d118451a-a046-4bbe-9819-f7722d7a5c1c")
                .withEncounter(encounterOne)
                .build();
        DrugOrder drugOrderTwo = DrugOrderBuilder.getBuilder("8a8ff846-98b3-48c6-88e7-5b734654a56a")
                .withEncounter(encounterTwo)
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
            add(encounterUUIDOne);
            add(encounterUUIDTwo);
        }};

        assertThat(actualEncounterUUIDs, is(expectedEncounterUUIDs));
    }

    @Test
    public void shouldGroupDrugOrdersByEncounterUUIDForEncountersHavingMultipleDrugOrders() {
        String encounterUUIDOne = "0a90531a-285c-438b-b265-bb3abb4745bd";
        String encounterUUIDTwo = "0b90531a-285c-238b-b485-bb3abb4745bd";

        Encounter encounterOne = EncounterBuilder.getBuilder(encounterUUIDOne).build();
        Encounter encounterTwo = EncounterBuilder.getBuilder(encounterUUIDTwo).build();

        String drugOrderOneUUID = "d118451a-a046-4bbe-9819-f7722d7a5c1c";
        String drugOrderTwoUUID = "8a8ff846-98b3-48c6-88e7-5b734654a56a";
        String drugOrderThreeUUID = "5c66b4c8-7086-47f6-a0ac-44c7b423695d";

        DrugOrder drugOrderOne = DrugOrderBuilder.getBuilder(drugOrderOneUUID)
                .withEncounter(encounterOne)
                .build();
        DrugOrder drugOrderTwo = DrugOrderBuilder.getBuilder(drugOrderTwoUUID)
                .withEncounter(encounterTwo)
                .build();
        DrugOrder drugOrderThree = DrugOrderBuilder.getBuilder(drugOrderThreeUUID)
                .withEncounter(encounterTwo)
                .build();

        List<DrugOrder> openMrsDrugOrders = new ArrayList<DrugOrder>() {{
            add(drugOrderOne);
            add(drugOrderTwo);
            add(drugOrderThree);
        }};

        DrugOrders drugOrders = new DrugOrders(openMrsDrugOrders);

        Map<Encounter, DrugOrders> encounterDrugOrdersMap = drugOrders.groupByEncounter();

        List<String> actualEncounterOneDrugOrderUUIDs = encounterDrugOrdersMap.get(encounterOne).stream().map(DrugOrder::getUuid).collect(Collectors.toList());
        List<String> actualEncounterTwoDrugOrderUUIDs = encounterDrugOrdersMap.get(encounterTwo).stream().map(DrugOrder::getUuid).collect(Collectors.toList());

        assertThat(actualEncounterOneDrugOrderUUIDs, is(singletonList(drugOrderOneUUID)));
        assertThat(actualEncounterTwoDrugOrderUUIDs, is(asList(drugOrderTwoUUID, drugOrderThreeUUID)));

    }
}