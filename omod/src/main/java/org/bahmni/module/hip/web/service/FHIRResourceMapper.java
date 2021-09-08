package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.OpenMrsCondition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Medication;
import org.openmrs.DrugOrder;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
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
import java.util.Set;

import static org.bahmni.module.hip.web.service.Constants.DOCUMENT_TYPE;
import static org.bahmni.module.hip.web.service.Constants.GIF;
import static org.bahmni.module.hip.web.service.Constants.IMAGE;
import static org.bahmni.module.hip.web.service.Constants.JPEG;
import static org.bahmni.module.hip.web.service.Constants.JPG;
import static org.bahmni.module.hip.web.service.Constants.MIMETYPE_IMAGE_JPEG;
import static org.bahmni.module.hip.web.service.Constants.MIMETYPE_PDF;
import static org.bahmni.module.hip.web.service.Constants.PATIENT_DOCUMENT;
import static org.bahmni.module.hip.web.service.Constants.PATIENT_DOCUMENTS_PATH;
import static org.bahmni.module.hip.web.service.Constants.PATIENT_DOCUMENT_TYPE;
import static org.bahmni.module.hip.web.service.Constants.PDF;
import static org.bahmni.module.hip.web.service.Constants.PNG;
import static org.bahmni.module.hip.web.service.Constants.RADIOLOGY_REPORT;
import static org.bahmni.module.hip.web.service.Constants.RADIOLOGY_TYPE;

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

    public DocumentReference mapToDocumentDocumentReference(Obs obs) {
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(obs.getUuid());
        List<DocumentReference.DocumentReferenceContentComponent> contents = new ArrayList<>();
        try {
            List<Attachment> attachments = getAttachments(obs);
            for (Attachment attachment : attachments) {
                DocumentReference.DocumentReferenceContentComponent documentReferenceContentComponent
                        = new DocumentReference.DocumentReferenceContentComponent();
                documentReferenceContentComponent.setAttachment(attachment);
                contents.add(documentReferenceContentComponent);
            }
            documentReference.setContent(contents);
            return documentReference;
        } catch (IOException exception) {
            return documentReference;
        }
    }

    private List<Attachment> getAttachments(Obs obs) throws IOException {
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = new Attachment();
        Set<Obs> obsList = obs.getGroupMembers();
        StringBuilder valueText = new StringBuilder();
        StringBuilder contentType = new StringBuilder();
        for(Obs obs1 : obsList){
            if(obs1.getConcept().getId().toString().equals(DOCUMENT_TYPE)){
                valueText.append(obs1.getValueText());
                contentType.append(getTypeOfTheObsDocument(obs1.getValueText()));
            }
        }
        attachment.setContentType(contentType.toString());
        byte[] fileContent = Files.readAllBytes(new File(PATIENT_DOCUMENTS_PATH + valueText).toPath());
        attachment.setData(fileContent);
        StringBuilder title = new StringBuilder();
        String encounterId = obs.getEncounter().getEncounterType().getEncounterTypeId().toString();
        if(encounterId.equals(PATIENT_DOCUMENT_TYPE))
            title.append(PATIENT_DOCUMENT);
        else if(encounterId.equals(RADIOLOGY_TYPE))
            title.append(RADIOLOGY_REPORT);
        title.append(": ").append(obs.getConcept().getName().getName());
        attachment.setTitle(title.toString());
        attachments.add(attachment);
        return attachments;
    }

    public Condition mapToCondition(OpenMrsCondition openMrsCondition) {
        Condition condition = new Condition();
        CodeableConcept concept = new CodeableConcept();
        concept.setText(openMrsCondition.getName());
        condition.setCode(concept);
        condition.setId(openMrsCondition.getUuid());
        condition.setRecordedDate(openMrsCondition.getRecordedDate());
        return condition;
    }

    public Observation mapToObs(Obs obs) {
        return observationTranslator.toFhirResource(obs);
    }

    public ServiceRequest mapToOrder(Order order){
        ServiceRequest serviceRequest = new ServiceRequest();
        CodeableConcept concept = new CodeableConcept();
        concept.setText(order.getConcept().getDisplayString());
        serviceRequest.setCode(concept);
        serviceRequest.setId(order.getUuid());
        return serviceRequest;
    }

    public Procedure mapToProcedure(Obs obs) {
        Procedure procedure = new Procedure();
        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        CodeableConcept concept = new CodeableConcept();
        concept.setText(obs.getValueCoded().getDisplayString());
        procedure.setCode(concept);
        procedure.setId(obs.getUuid());
        return procedure;
    }

    private String getTypeOfTheObsDocument(String valueText) {
        if (valueText == null) return "";
        String extension = valueText.substring(valueText.indexOf('.') + 1);
        if (extension.compareTo(JPEG) == 0 || extension.compareTo(JPG) == 0) {
            return MIMETYPE_IMAGE_JPEG;
        } else if (extension.compareTo(PNG) == 0 || extension.compareTo(GIF) == 0) {
            return IMAGE + extension;
        } else if (extension.compareTo(PDF) == 0) {
            return MIMETYPE_PDF;
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
