package org.bahmni.module.hip.web.model;

import org.bahmni.module.hip.web.service.FHIRResourceMapper;
import org.bahmni.module.hip.web.service.FHIRUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Observation;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FhirDiagnosticReport {
    private final List<Observation> observations;
    private final Date encounterTimestamp;
    private final Integer encounterID;

    private FhirDiagnosticReport(Date encounterDatetime, List<Observation> observations, Integer encounterID) {
        this.encounterTimestamp = encounterDatetime;
        this.observations = observations;
        this.encounterID = encounterID;
    }


    public Bundle bundleDiagnosticReport(String webUrl) {
        String bundleID = String.format("PR-%d", encounterID);
        Bundle bundle = FHIRUtils.createBundle(encounterTimestamp, bundleID, webUrl);

        FHIRUtils.addToBundleEntry(bundle, observations, false);
        return bundle;
    }

    public static FhirDiagnosticReport fromOpenMrsDiagnosticReport(OpenMrsDiagnosticReport openMrsDiagnosticReport,
                                                                   FHIRResourceMapper fhirResourceMapper) {
        Date encounterDatetime = openMrsDiagnosticReport.getEncounter().getEncounterDatetime();
        Integer encounterId = openMrsDiagnosticReport.getEncounter().getId();
        List<Observation> observations = openMrsDiagnosticReport.getEncounter().getAllObs().stream()
                .map(fhirResourceMapper::mapToObs).collect(Collectors.toList());
        for (Observation o : observations) {
            String valueText = o.getValueStringType().getValueAsString();
            o.getValueStringType().setValueAsString("/document_images/" + valueText);
        }
        return new FhirDiagnosticReport(encounterDatetime, observations, encounterId);
    }
}
