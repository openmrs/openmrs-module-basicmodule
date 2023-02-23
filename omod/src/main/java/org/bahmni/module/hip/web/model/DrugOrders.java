package org.bahmni.module.hip.web.model;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DrugOrders {

    private List<DrugOrder> openMRSDrugOrders;

    public DrugOrders(@NotNull List<DrugOrder> openMRSDrugOrders) {
        this.openMRSDrugOrders = openMRSDrugOrders;
    }

    public Boolean isEmpty(){
        return CollectionUtils.isEmpty(openMRSDrugOrders);
    }

    public Map<Encounter, DrugOrders> groupByEncounter(){
        return groupByEncounterUUID()
                .values()
                .stream()
                .map(DrugOrders::new)
                .collect(Collectors.toMap(DrugOrders::firstEncounter, drugOrders -> drugOrders));
    }

    public int size() {
        return openMRSDrugOrders.size();
    }

    private Map<String, List<DrugOrder>> groupByEncounterUUID(){
        return openMRSDrugOrders
                .stream()
                .collect(Collectors.groupingBy(order -> order.getEncounter().getUuid()));
    }

    private Encounter firstEncounter() {
        return openMRSDrugOrders.get(0).getEncounter();
    }

    Stream<DrugOrder> stream(){
        return openMRSDrugOrders.stream();
    }

    public List<DrugOrder> getOpenMRSDrugOrders() {
        return openMRSDrugOrders;
    }
}
