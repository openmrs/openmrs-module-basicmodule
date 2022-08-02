package org.bahmni.module.hip.web.model;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class OpenMrsLabResults {
    private Encounter encounter;
    private List<LabOrderResult> labOrderResults;
    private Patient patient;
    private final Set<EncounterProvider> encounterProviders;
    private final Map<Obs , String> observationsWithTestName;


    public OpenMrsLabResults(@NotEmpty Encounter encounter, Patient patient, List<LabOrderResult> labOrderResults, Map<Obs, String> observationsWithTestName) {
        this.encounter = encounter;
        this.patient = patient;
        this.labOrderResults = labOrderResults;
        this.encounterProviders = encounter.getEncounterProviders();
        this.observationsWithTestName = observationsWithTestName;
    }


    public static List<OpenMrsLabResults> from(Map<Order, List<LabOrderResult>> labOrderResultsMap, Map<Order,Map<Obs, String>> labReportDocuments) {
        return labOrderResultsMap
                .entrySet()
                .stream()
                .map(entry -> new OpenMrsLabResults(entry.getKey().getEncounter(), entry.getKey().getPatient(), entry.getValue(),labReportDocuments.get(entry)))
                .collect(Collectors.toList());
    }
}
