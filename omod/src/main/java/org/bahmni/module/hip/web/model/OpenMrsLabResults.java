package org.bahmni.module.hip.web.model;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class OpenMrsLabResults {
    private Encounter encounter;
    private List<LabOrderResult> labOrderResults;
    private Patient patient;

    public OpenMrsLabResults(@NotEmpty Encounter encounter, Patient patient, List<LabOrderResult> labOrderResults) {
        this.encounter = encounter;
        this.patient = patient;
        this.labOrderResults = labOrderResults;
    }


    public static List<OpenMrsLabResults> from(Map<Order, List<LabOrderResult>> labOrderResultsMap) {
        return labOrderResultsMap
                .entrySet()
                .stream()
                .map(entry -> new OpenMrsLabResults(entry.getKey().getEncounter(), entry.getKey().getPatient(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
