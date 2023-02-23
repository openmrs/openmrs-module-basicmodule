package org.bahmni.module.hip.web.model;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Patient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class OpenMrsDiagnosticReport {
    private final Encounter encounter;
    private final List<Obs> obs;
    private final Patient patient;
    private final Set<EncounterProvider> encounterProviders;

    private OpenMrsDiagnosticReport(@NotEmpty Encounter encounter, List<Obs> obs) {
        this.encounter = encounter;
        this.obs = obs;
        this.patient = encounter.getPatient();
        this.encounterProviders = encounter.getEncounterProviders();
    }

    public static List<OpenMrsDiagnosticReport> fromDiagnosticReport(Map<Encounter, List<Obs>> encounterDrugOrdersMap) {
        return encounterDrugOrdersMap
                .entrySet()
                .stream()
                .map(entry -> new OpenMrsDiagnosticReport(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
