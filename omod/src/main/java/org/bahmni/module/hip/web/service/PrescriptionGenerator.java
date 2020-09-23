package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.Prescription;
import org.hibernate.validator.constraints.NotEmpty;
import org.hl7.fhir.r4.model.*;
import org.openmrs.DrugOrder;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionGenerator {
    private final CareContextService careContextService;

    @Autowired
    public PrescriptionGenerator(CareContextService careContextService) {
        this.careContextService = careContextService;
    }

    public Prescription generate(@NotEmpty List<DrugOrder> drugOrders) {
        org.openmrs.Encounter emrEncounter = drugOrders.get(0).getEncounter();
        OrgContext orgContext = getOrgContext();
        Bundle prescriptionBundle = createPrescriptionBundle(emrEncounter, orgContext, drugOrders);

        return Prescription.builder()
                .bundle(prescriptionBundle)
                .careContext(careContextService.careContextFor(emrEncounter, orgContext.getCareContextType()))
                .build();
    }

    private Bundle createPrescriptionBundle(org.openmrs.Encounter emrEncounter, OrgContext orgContext, List<DrugOrder> drugOrders) {
        org.openmrs.Patient emrPatient = emrEncounter.getPatient();


        Bundle bundle = FHIRUtils.createBundle(emrEncounter.getEncounterDatetime(), prescriptionId(emrEncounter), orgContext);

        //Plain composition initialized
        Composition composition = initializeComposition(orgContext.getWebUrl(), emrEncounter.getEncounterDatetime());
        Composition.SectionComponent compositionSection = composition.addSection();

        //Construct practitioners
        List<Practitioner> practitioners = getPractitionersFrom(emrEncounter);

        //add patient to bundle and the ref to composition.subject
        Patient patientResource = FHIRResourceMapper.mapToPatient(emrPatient);
        Reference patientRef = FHIRUtils.getReferenceToResource(patientResource);

        //add encounter to bundle and ref to composition
        Encounter fhirEncounter = FHIRResourceMapper
                .mapToEncounter(emrEncounter, composition.getDate())
                .setSubject(patientRef);

        FHIRUtils.addToBundleEntry(bundle, composition, false);
        FHIRUtils.addToBundleEntry(bundle, practitioners, false);
        FHIRUtils.addToBundleEntry(bundle, fhirEncounter, false);
        FHIRUtils.addToBundleEntry(bundle, patientResource, false);

        //Populate Composition
        composition.setEncounter(FHIRUtils.getReferenceToResource(fhirEncounter));
        composition.setSubject(patientRef);

        compositionSection
        .setTitle("OPD Prescription")
        .setCode(FHIRUtils.getPrescriptionType());

        practitioners
                .forEach(practitioner -> composition
                        .addAuthor()
                        .setResource(practitioner)
                        .setDisplay(FHIRUtils.getDisplay(practitioner))
                );

        for (DrugOrder order: drugOrders) {
            Medication medication = FHIRResourceMapper.mapToMedication(order);
            if (medication != null) {
                FHIRUtils.addToBundleEntry(bundle, medication, false);
            }
            MedicationRequest medicationRequest = FHIRResourceMapper.mapToMedicationRequest(order, patientRef, composition.getAuthorFirstRep().getResource(), medication);
            FHIRUtils.addToBundleEntry(bundle, medicationRequest, false);
            compositionSection.getEntry().add(FHIRUtils.getReferenceToResource(medicationRequest));
        }
        return bundle;
    }

    private List<Practitioner> getPractitionersFrom(org.openmrs.Encounter emrEncounter) {
        return emrEncounter.getEncounterProviders()
                .stream()
                .map(FHIRResourceMapper::mapToPractitioner)
                .collect(Collectors.toList());
    }

    private Composition initializeComposition(String webUrl, Date encounterTimestamp) {
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

    private OrgContext getOrgContext() {
        Organization organization = getOrganization();
        return OrgContext.builder()
                .organization(organization)
                .webUrl(getWebUrl())
                .build();
    }

    private String getWebUrl() {
        AdministrationService administrationService = Context.getAdministrationService();
        return administrationService.getGlobalProperty(Constants.PROP_HFR_URL);
    }

    private Organization getOrganization() {
        AdministrationService administrationService = Context.getAdministrationService();
        String hfrId = administrationService.getGlobalProperty(Constants.PROP_HFR_ID);
        String hfrName = administrationService.getGlobalProperty(Constants.PROP_HFR_NAME);
        String hfrSystem = administrationService.getGlobalProperty(Constants.PROP_HFR_SYSTEM);
        return FHIRUtils.createOrgInstance(hfrId, hfrName, hfrSystem);
    }

}
