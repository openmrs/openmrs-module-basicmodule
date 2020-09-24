package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.FhirPrescription;
import org.bahmni.module.hip.web.model.OpenMrsPrescription;
import org.hl7.fhir.r4.model.*;
import org.openmrs.DrugOrder;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionGenerator {
    private final CareContextService careContextService;

    @Autowired
    public PrescriptionGenerator(CareContextService careContextService) {
        this.careContextService = careContextService;
    }

    BundledPrescriptionResponse generate(OpenMrsPrescription openMrsPrescription) {

        Bundle prescriptionBundle = createPrescriptionBundle(openMrsPrescription);

        return BundledPrescriptionResponse.builder()
                .bundle(prescriptionBundle)
                .careContext(careContextService.careContextFor(openMrsPrescription.getEncounter(), getOrgContext().getCareContextType()))
                .build();
    }

    private Bundle createPrescriptionBundle(OpenMrsPrescription openMrsPrescription) {

        FhirPrescription fhirPrescription = FhirPrescription.from(openMrsPrescription);

        //Plain composition initialized
        Composition composition = initializeComposition(openMrsPrescription.getEncounter());
        Composition.SectionComponent compositionSection = composition.addSection();

        fhirPrescription.getPractitioners()
                .forEach(practitioner -> composition
                        .addAuthor().setResource(practitioner).setDisplay(FHIRUtils.getDisplay(practitioner)));

        composition
                .setEncounter(FHIRUtils.getReferenceToResource(fhirPrescription.getEncounter()))
                .setSubject(fhirPrescription.getPatientReference());

        compositionSection
                .setTitle("OPD Prescription")
                .setCode(FHIRUtils.getPrescriptionType());

        fhirPrescription.getMedicationRequests()
                .stream()
                .map(FHIRUtils::getReferenceToResource)
                .forEach(compositionSection::addEntry);

        return wrapInBundle(openMrsPrescription, fhirPrescription, composition);
    }

    private Bundle wrapInBundle(OpenMrsPrescription openMrsPrescription, FhirPrescription fhirPrescription, Composition composition) {
        Bundle bundle = initializeBundle(openMrsPrescription.getEncounter());

        FHIRUtils.addToBundleEntry(bundle, composition, false);
        FHIRUtils.addToBundleEntry(bundle, fhirPrescription.getEncounter(), false);
        FHIRUtils.addToBundleEntry(bundle, fhirPrescription.getPractitioners(), false);
        FHIRUtils.addToBundleEntry(bundle, fhirPrescription.getPatient(), false);
        FHIRUtils.addToBundleEntry(bundle, fhirPrescription.getMedications(), false);
        FHIRUtils.addToBundleEntry(bundle, fhirPrescription.getMedicationRequests(), false);
        return bundle;
    }

    private Bundle initializeBundle(org.openmrs.Encounter emrEncounter) {
        OrgContext orgContext = getOrgContext();
        return FHIRUtils.createBundle(emrEncounter.getEncounterDatetime(), prescriptionId(emrEncounter), orgContext);
    }

    private List<MedicationRequest> medicationRequestsFor(List<DrugOrder> drugOrders, Practitioner practitioner, Reference patientRef) {
        return drugOrders
                .stream()
                .map(drugOrder -> FHIRResourceMapper
                        .mapToMedicationRequest(drugOrder, patientRef, practitioner, FHIRResourceMapper.mapToMedication(drugOrder)))
                .collect(Collectors.toList());
    }

    private List<Medication> fhirMedicationFor(List<DrugOrder> drugOrders) {
        return drugOrders
                .stream()
                .map(FHIRResourceMapper::mapToMedication)
                .filter(medication -> !Objects.isNull(medication))
                .collect(Collectors.toList());
    }

    private List<Practitioner> getPractitionersFrom(org.openmrs.Encounter emrEncounter) {
        return emrEncounter.getEncounterProviders()
                .stream()
                .map(FHIRResourceMapper::mapToPractitioner)
                .collect(Collectors.toList());
    }

    private Composition initializeComposition(org.openmrs.Encounter encounterTimestamp) {
        OrgContext orgContext = getOrgContext();

        Composition composition = new Composition();
        composition.setId(UUID.randomUUID().toString());
        composition.setDate(encounterTimestamp.getEncounterDatetime());
        composition.setIdentifier(FHIRUtils.getIdentifier(composition.getId(), orgContext.getWebUrl(), "document"));
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
