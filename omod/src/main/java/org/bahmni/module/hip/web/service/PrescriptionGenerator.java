package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.Prescription;
import org.hibernate.validator.constraints.NotEmpty;
import org.hl7.fhir.r4.model.*;
import org.openmrs.DrugOrder;
import org.openmrs.VisitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionGenerator {
    private final CareContextService careContextService;
    private final BahmniAdministrationService bahmniAdministrationService;

    @Autowired
    public PrescriptionGenerator(CareContextService careContextService, BahmniAdministrationService bahmniAdministrationService) {
        this.careContextService = careContextService;
        this.bahmniAdministrationService = bahmniAdministrationService;
    }

    public Prescription generate(@NotEmpty List<DrugOrder> drugOrders) {
        org.openmrs.Encounter emrEncounter = drugOrders.get(0).getEncounter();
        Bundle prescriptionBundle = createPrescriptionBundle(emrEncounter, drugOrders);

        // todo: refactor hardcoding VisitType
        return Prescription.builder()
                .bundle(prescriptionBundle)
                .careContext(careContextService.careContextFor(emrEncounter, VisitType.class))
                .build();
    }

    private Bundle createPrescriptionBundle(org.openmrs.Encounter emrEncounter, List<DrugOrder> drugOrders) {
        String prescriptionId = prescriptionId(emrEncounter);
        org.openmrs.Patient emrPatient = emrEncounter.getPatient();
        Date encounterTimestamp = emrEncounter.getEncounterDatetime();
        Bundle bundle = FHIRUtils.createBundle(encounterTimestamp, prescriptionId, getWebUrl());

        Patient patientResource = FHIRResourceMapper.mapToPatient(emrPatient);
        Reference patientRef = FHIRUtils.getReferenceToResource(patientResource);

        //add composition first
        Composition composition = getComposition(getWebUrl(), encounterTimestamp);
        FHIRUtils.addToBundleEntry(bundle, composition, false);

        //add practitioner to bundle to composition.author
        List<Practitioner> practitioners = emrEncounter.getEncounterProviders()
                .stream()
                .map(FHIRResourceMapper::mapToPractitioner)
                .collect(Collectors.toList());

        practitioners.forEach(practitioner -> {
            FHIRUtils.addToBundleEntry(bundle, practitioner, false);
            Reference authorRef = composition.addAuthor();
            authorRef.setResource(practitioner);
            authorRef.setDisplay(FHIRUtils.getDisplay(practitioner));
        });

        //add patient to bundle and the ref to composition.subject
        FHIRUtils.addToBundleEntry(bundle, patientResource, false);
        composition.setSubject(patientRef);

        //add encounter to bundle and ref to composition
        Encounter encounter = FHIRResourceMapper.mapToEncounter(emrEncounter, composition.getDate());
        encounter.setSubject(patientRef);
        FHIRUtils.addToBundleEntry(bundle, encounter, false);
        composition.setEncounter(FHIRUtils.getReferenceToResource(encounter));

        Composition.SectionComponent section = composition.addSection();
        section.setTitle("OPD Prescription");
        section.setCode(FHIRUtils.getPrescriptionType());

        for (DrugOrder order: drugOrders) {
            Medication medication = FHIRResourceMapper.mapToMedication(order);
            if (medication != null) {
                FHIRUtils.addToBundleEntry(bundle, medication, false);
            }
            MedicationRequest medicationRequest = FHIRResourceMapper.mapToMedicationRequest(order, patientRef, composition.getAuthorFirstRep().getResource(), medication);
            FHIRUtils.addToBundleEntry(bundle, medicationRequest, false);
            section.getEntry().add(FHIRUtils.getReferenceToResource(medicationRequest));
        }
        return bundle;
    }

    private Composition getComposition(String webUrl, Date encounterTimestamp) {
        Composition composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        composition.setDate(encounterTimestamp);
        composition.setIdentifier(FHIRUtils.getIdentifier(composition.getId(), webUrl, "document"));
        composition.setStatus(Composition.CompositionStatus.FINAL);
        composition.setType(FHIRUtils.getPrescriptionType());
        composition.setTitle("Prescription");
        return composition;
    }

    private String prescriptionId(org.openmrs.Encounter emrEncounter) {
        return "PR-" + emrEncounter.getEncounterId().toString();
    }

    private String getWebUrl() {
        return bahmniAdministrationService.getWebUrl();
    }

}
