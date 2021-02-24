package org.bahmni.module.hip.web.model;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import org.openmrs.Encounter;
import org.openmrs.Obs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Getter
public class OpenMrsDiagnosticReport {
    private Encounter encounter;
    private List<Obs> obs;

    private OpenMrsDiagnosticReport(@NotEmpty Encounter encounter, List<Obs> obs) {
        this.encounter = encounter;
        this.obs = obs;
    }
    public static List<OpenMrsDiagnosticReport> fromDiagnosticReport(Map<Encounter, List<Obs>> encounterDrugOrdersMap) {
        return encounterDrugOrdersMap
                .entrySet()
                .stream()
                .map(entry -> new OpenMrsDiagnosticReport(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
