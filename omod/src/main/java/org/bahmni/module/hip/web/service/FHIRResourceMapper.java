package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.DrugOrder;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.impl.EncounterTranslatorImpl;
import org.openmrs.module.fhir2.api.translators.impl.ObservationTranslatorImpl;
import org.openmrs.module.fhir2.api.translators.impl.PractitionerTranslatorProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FHIRResourceMapper {

    private final PatientTranslator patientTranslator;
    private final PractitionerTranslatorProviderImpl practitionerTranslatorProvider;
    private final MedicationRequestTranslator medicationRequestTranslator;
    private final MedicationTranslator medicationTranslator;
    private final EncounterTranslatorImpl encounterTranslator;
    private final ObservationTranslatorImpl observationTranslator;

    @Autowired
    public FHIRResourceMapper(PatientTranslator patientTranslator, PractitionerTranslatorProviderImpl practitionerTranslatorProvider, MedicationRequestTranslator medicationRequestTranslator, MedicationTranslator medicationTranslator, EncounterTranslatorImpl encounterTranslator, ObservationTranslatorImpl observationTranslator) {
        this.patientTranslator = patientTranslator;
        this.practitionerTranslatorProvider = practitionerTranslatorProvider;
        this.medicationRequestTranslator = medicationRequestTranslator;
        this.medicationTranslator = medicationTranslator;
        this.encounterTranslator = encounterTranslator;
        this.observationTranslator = observationTranslator;
    }

    public Encounter mapToEncounter(org.openmrs.Encounter emrEncounter) {
        return encounterTranslator.toFhirResource(emrEncounter);
    }

    public DiagnosticReport mapToDiagnosticReport(Obs obs) {
        DiagnosticReport diagnosticReport = new DiagnosticReport();
        try {
            diagnosticReport.setId(obs.getUuid());
            diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
            diagnosticReport.setPresentedForm(getAttachments(obs));

            return diagnosticReport;
        } catch (IOException exception) {
            return diagnosticReport;
        }
    }

    private List<Attachment> getAttachments(Obs obs) throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = new Attachment();
        attachment.setContentType(getTypeOfTheObsDocument(obs.getValueText()));
        byte[] fileContent = Files.readAllBytes(new File(Constants.PATIENT_DOCUMENTS_PATH + obs.getValueText()).toPath());
        attachment.setData(fileContent);
        attachments.add(attachment);
        return attachments;
    }

    public Observation mapToObs(Obs obs) {
        return observationTranslator.toFhirResource(obs);
    }

    private String getTypeOfTheObsDocument(String valueText) {
        if (valueText == null) return "";
        String extension = valueText.substring(valueText.indexOf('.') + 1);
        if (extension.compareTo(Constants.JPEG) == 0 || extension.compareTo(Constants.JPG) == 0) {
            return Constants.MIMETYPE_IMAGE_JPEG;
        } else if (extension.compareTo(Constants.PNG) == 0 || extension.compareTo(Constants.GIF) == 0) {
            return Constants.IMAGE + extension;
        } else if (extension.compareTo(Constants.PDF) == 0) {
            return Constants.MIMETYPE_PDF;
        } else {
            return "";
        }
    }

    public Patient mapToPatient(org.openmrs.Patient emrPatient) {
        return patientTranslator.toFhirResource(emrPatient);
    }

    public Practitioner mapToPractitioner(EncounterProvider encounterProvider) {
        return practitionerTranslatorProvider.toFhirResource(encounterProvider.getProvider());
    }

    private String displayName(Object object) {
        if (object == null)
            return "";
        return object.toString() + " ";

    }

    public MedicationRequest mapToMedicationRequest(DrugOrder order) {
        String dosingInstrutions = displayName(order.getDose()) +
                displayName(order.getDoseUnits() == null ? "" : order.getDoseUnits().getName()) +
                displayName(order.getFrequency()) +
                displayName(order.getRoute() == null ? "" : order.getRoute().getName()) +
                displayName(order.getDuration()) +
                displayName(order.getDurationUnits() == null ? "" : order.getDurationUnits().getName());
        MedicationRequest medicationRequest = medicationRequestTranslator.toFhirResource(order);
        Dosage dosage = medicationRequest.getDosageInstruction().get(0);
        dosage.setText(dosingInstrutions);
        return medicationRequest;
    }

    public Medication mapToMedication(DrugOrder order) {
        if (order.getDrug() == null) {
            return null;
        }
        return medicationTranslator.toFhirResource(order.getDrug());
    }
}
