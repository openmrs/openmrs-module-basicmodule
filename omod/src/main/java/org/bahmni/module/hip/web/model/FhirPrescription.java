package org.bahmni.module.hip.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bahmni.module.hip.web.service.FHIRResourceMapper;
import org.bahmni.module.hip.web.service.FHIRUtils;
import org.hl7.fhir.r4.model.*;
import org.openmrs.DrugOrder;
import org.openmrs.EncounterProvider;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class FhirPrescription {

    private Encounter encounter;
    private List<Practitioner> practitioners;
    private Patient patient;
    private Reference patientReference;
    private List<Medication> medications;
    private List<MedicationRequest> medicationRequests;

    private FhirPrescription(Encounter encounter, List<Practitioner> practitioners, Patient patient, Reference patientReference, List<Medication> medications, List<MedicationRequest> medicationRequests) {
        this.encounter = encounter;
        this.practitioners = practitioners;
        this.patient = patient;
        this.patientReference = patientReference;
        this.medications = medications;
        this.medicationRequests = medicationRequests;
    }

    public static FhirPrescription from(OpenMrsPrescription openMrsPrescription) {
        Patient patient = FHIRResourceMapper.mapToPatient(openMrsPrescription.getPatient());
        Reference patientReference = FHIRUtils.getReferenceToResource(patient);
        Encounter encounter = encounterFrom(openMrsPrescription.getEncounter(), patientReference);
        List<Practitioner> practitioners = getPractitionersFrom(openMrsPrescription.getEncounterProviders());
        List<MedicationRequest> medicationRequests = medicationRequestsFor(openMrsPrescription.getDrugOrders(), patientReference, practitioners.get(0));
        List<Medication> medications = medicationsFor(openMrsPrescription.getDrugOrders());

        return new FhirPrescription(encounter, practitioners, patient, patientReference, medications, medicationRequests);
    }

    private static Encounter encounterFrom(org.openmrs.Encounter openMRSEncounter, Reference patientReference){
        return FHIRResourceMapper
                .mapToEncounter(openMRSEncounter, openMRSEncounter.getEncounterDatetime())
                .setSubject(patientReference);
    }

    private static List<MedicationRequest> medicationRequestsFor(
            List<DrugOrder> drugOrders,
            Reference patientReference,
            Practitioner practitioner) {
        return drugOrders
                .stream()
                .map(drugOrder -> {
                    Medication medication = FHIRResourceMapper.mapToMedication(drugOrder);
                    return FHIRResourceMapper.mapToMedicationRequest(
                            drugOrder,
                            patientReference,
                            practitioner,
                            medication
                    );
                })
                .collect(Collectors.toList());
    }

    private static List<Medication> medicationsFor(List<DrugOrder> drugOrders) {
        return drugOrders
                .stream()
                .map(FHIRResourceMapper::mapToMedication)
                .filter(medication -> !Objects.isNull(medication))
                .collect(Collectors.toList());
    }

    private static List<Practitioner> getPractitionersFrom(Set<EncounterProvider> encounterProviders) {
        return encounterProviders
                .stream()
                .map(FHIRResourceMapper::mapToPractitioner)
                .collect(Collectors.toList());
    }
}
