package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.CareContext;
import org.bahmni.module.hip.web.model.Prescription;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.openmrs.DrugOrder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PrescriptionGenerator {
    private final OrgContext orgContext;
    private final org.openmrs.Encounter emrEncounter;
    private final List<DrugOrder> drugOrders;

    public PrescriptionGenerator(OrgContext orgContext, org.openmrs.Encounter emrEncounter, List<DrugOrder> drugOrders) {
        this.orgContext = orgContext;
        this.emrEncounter = emrEncounter;
        this.drugOrders = drugOrders;
    }

    public Prescription generate() throws Exception {
        Bundle prescriptionBundle = createPrescriptionBundle(emrEncounter.getPatient(), orgContext);
        return Prescription.builder()
                .bundle(prescriptionBundle)
                .careContext(getCareContext(emrEncounter))
                .build();
    }

    private CareContext getCareContext(org.openmrs.Encounter emrEncounter) {
        Class cls = orgContext.getCareContextType();
        if (cls.getName().equals("Visit")) {
            return CareContext.builder()
                    .careContextReference(emrEncounter.getVisit().getUuid())
                    .careContextType("Visit").build();
        } else {
            return CareContext.builder()
                    .careContextReference(emrEncounter.getVisit().getVisitType().getName())
                    .careContextType("VisitType").build();
        }
    }

    private Bundle createPrescriptionBundle(org.openmrs.Patient emrPatient, OrgContext hipContext) throws Exception {
        String prescriptionId = prescriptionId();
        Bundle bundle = FHIRUtils.createBundle(emrEncounter.getEncounterDatetime(), prescriptionId, orgContext);

        Patient patientResource = FHIRResourceMapper.mapToPatient(emrPatient);
        Reference patientRef = FHIRUtils.getReferenceToResource(patientResource);

        //add composition first
        Composition composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        composition.setDate(bundle.getTimestamp());
        composition.setIdentifier(FHIRUtils.getIdentifier(composition.getId(), hipContext.getWebUrl(), "document"));
        composition.setStatus(Composition.CompositionStatus.FINAL);
        CodeableConcept prescriptionType = FHIRUtils.getPrescriptionType();
        composition.setType(prescriptionType);
        composition.setTitle("Prescription");
        FHIRUtils.addToBundleEntry(bundle, composition, false);

        //add practitioner to bundle to composition.author
        List<Practitioner> practitioners = emrEncounter.getEncounterProviders().stream().map(provider -> {
            return FHIRResourceMapper.mapToPractitioner(provider);
        }).collect(Collectors.toList());

        practitioners.stream().forEach(practitioner -> {
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
        section.setCode(prescriptionType);

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

    private String prescriptionId() {
        return "PR-" + emrEncounter.getEncounterId().toString();
    }


}
