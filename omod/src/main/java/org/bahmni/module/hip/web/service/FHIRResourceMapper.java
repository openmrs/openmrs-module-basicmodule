package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.web.model.OpenMrsCondition;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.MarkdownType;

import org.openmrs.DrugOrder;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Concept;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.MedicationTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.impl.EncounterTranslatorImpl;
import org.openmrs.module.fhir2.api.translators.impl.ObservationTranslatorImpl;
import org.openmrs.module.fhir2.api.translators.impl.PractitionerTranslatorProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class FHIRResourceMapper {

    private final PatientTranslator patientTranslator;
    private final PractitionerTranslatorProviderImpl practitionerTranslatorProvider;
    private final MedicationRequestTranslator medicationRequestTranslator;
    private final MedicationTranslator medicationTranslator;
    private final EncounterTranslatorImpl encounterTranslator;
    private final ObservationTranslatorImpl observationTranslator;
    public static Set<String> conceptNames = new HashSet<>(Arrays.asList("Follow up Date", "Additional Advice on Discharge", "Discharge Summary, Plan for follow up"));

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

    public Procedure mapToProcedure(Obs obs) {
        Procedure procedure = new Procedure();
        procedure.setId(obs.getUuid());
        procedure = obs.getGroupMembers().size() > 0 ? mapGroupMembersToProcedure(obs.getGroupMembers(), procedure)
                                                     : mapObsToProcedure(obs, procedure);
        return procedure;
    }

    public Procedure mapGroupMembersToProcedure(Set<Obs> obsGroupMembers, Procedure procedure){
        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        StringBuilder title = new StringBuilder();
        StringBuilder description = new StringBuilder("");
        CodeableConcept concept = new CodeableConcept();
        for(Obs o : obsGroupMembers){
            if(Objects.equals(o.getConcept().getName().getName(), "Procedure Notes, Procedure")){
                title.append(o.getValueCoded().getDisplayString());
            } else {
                description.append(description.toString().equals("") ? "" : ", ");
                if(o.getValueCoded() != null){
                    description.append(o.getValueCoded().getName().getName());
                }else if(o.getValueText() != null){
                    description.append(o.getValueText());
                } else if(o.getValueNumeric() != null){
                    description.append(o.getValueNumeric());
                }
            }
        }
        concept.setText(title + ", " + description);
        procedure.setCode(concept);
        return procedure;
    }

    public Procedure mapObsToProcedure(Obs obs, Procedure procedure){
        procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
        CodeableConcept concept = new CodeableConcept();
        concept.setText(obs.getValueCoded().getDisplayString());
        procedure.setCode(concept);
        return procedure;
    }

    public CarePlan mapToCarePlan(Obs obs){
        List<Obs> groupMembers = new ArrayList<>();
        getGroupMembersOfObs(obs, groupMembers);
        CarePlan carePlan = new CarePlan();
        carePlan.setId(obs.getUuid());
        String description = "";
        for(Obs o : groupMembers){
            if(o.getValueDatetime() != null) {
                description += description != "" ? ", " : "";
                description += o.getValueDatetime();
            } else if(o.getValueText() != null && Objects.equals(o.getConcept().getName().getName(), "Additional Advice on Discharge")){
                description += description != "" ? ", " : "";
                description +=  o.getValueText();
            }
            if(o.getValueText() != null && Objects.equals(o.getConcept().getName().getName(), "Discharge Summary, Plan for follow up")){
                carePlan.setTitle(o.getValueText());
                carePlan.setUserData("Discharge Summary, Plan for follow up", o.getValueText());
            }
        }
        if(!description.isEmpty()){
            carePlan.setDescription(description);
        }
        return carePlan;
    }

    private void getGroupMembersOfObs(Obs obs, List<Obs> groupMembers) {
        if (obs.getGroupMembers().size() > 0) {
            for (Obs groupMember : obs.getGroupMembers()) {
                if (conceptNames.contains(groupMember.getConcept().getDisplayString())){
                    groupMembers.add(groupMember);
                }
            }
        }
    }

    public DocumentReference mapToDocumentDocumentReference(Obs obs) {
        DocumentReference documentReference = new DocumentReference();
        documentReference.setId(obs.getUuid());
        documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
        documentReference.setDescription(obs.getComment());
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
        StringBuilder valueText = new StringBuilder();
        Set<Obs> obsList = obs.getGroupMembers();
        StringBuilder contentType = new StringBuilder();
        for(Obs obs1 : obsList) {
            if (obs1.getConcept().getName().getName().equals(Config.DOCUMENT_TYPE.getValue())) {
                valueText.append(obs1.getValueText());
                contentType.append(FHIRUtils.getTypeOfTheObsDocument(obs1.getValueText()));
            }
        }
        if(obs.getConcept().getName().getName().equals(Config.IMAGE.getValue()) || obs.getConcept().getName().getName().equals(Config.PATIENT_VIDEO.getValue())){
            valueText.append(obs.getValueComplex());
            contentType.append(FHIRUtils.getTypeOfTheObsDocument(obs.getValueComplex()));
        }
        attachment.setContentType(contentType.toString());
        byte[] fileContent = Files.readAllBytes(new File(Config.PATIENT_DOCUMENTS_PATH.getValue() + valueText).toPath());
        attachment.setData(fileContent);
        StringBuilder title = new StringBuilder();
        String encounterId = obs.getEncounter().getEncounterType().getName();
        if(encounterId.equals(Config.PATIENT_DOCUMENT.getValue()))
            title.append(Config.PATIENT_DOCUMENT.getValue());
        else if(encounterId.equals(Config.RADIOLOGY_TYPE.getValue()))
            title.append(Config.RADIOLOGY_REPORT.getValue());
        else if(encounterId.equals(Config.CONSULTATION.getValue()))
            title.append("Consultation");
        title.append(": ").append(obs.getConcept().getName().getName());
        attachment.setTitle(title.toString());
        attachments.add(attachment);
        return attachments;
    }

    public Condition mapToCondition(OpenMrsCondition openMrsCondition, Patient patient) {
        Condition condition = new Condition();
        CodeableConcept concept = new CodeableConcept();
        concept.setText(openMrsCondition.getName());
        condition.setCode(concept);
        condition.setSubject(new Reference("Patient/" + patient.getId()));
        condition.setId(openMrsCondition.getUuid());
        condition.setRecordedDate(openMrsCondition.getRecordedDate());
        condition.setClinicalStatus(new CodeableConcept(new Coding().setCode("active").setSystem(Constants.FHIR_CONDITION_CLINICAL_STATUS_SYSTEM)));
        return condition;
    }

    public Observation mapToObs(Obs obs) {
        Concept concept = initializeEntityAndUnproxy(obs.getConcept());
        obs.setConcept(concept);
        Observation observation = observationTranslator.toFhirResource(obs);
        observation.addNote(new Annotation(new MarkdownType(obs.getComment())));
        return observation;
    }

    public ServiceRequest mapToOrder(Order order){
        ServiceRequest serviceRequest = new ServiceRequest();
        CodeableConcept concept = new CodeableConcept();
        concept.setText(order.getConcept().getDisplayString());
        serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.ORDER);
        serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
        serviceRequest.setSubject(new Reference("Patient/"+ order.getPatient().getUuid()));
        serviceRequest.setCode(concept);
        serviceRequest.setId(order.getUuid());
        return serviceRequest;
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
        medicationRequest.setSubject(new Reference("Patient/"+ order.getPatient().getUuid()));
        Dosage dosage = medicationRequest.getDosageInstruction().get(0);
        dosage.setText(dosingInstrutions.trim());
        return medicationRequest;
    }

    public Medication mapToMedication(DrugOrder order) {
        if (order.getDrug() == null) {
            return null;
        }
        return medicationTranslator.toFhirResource(order.getDrug());
    }

    public static <T> T initializeEntityAndUnproxy(T entity) {
        if (entity == null) {
            throw new NullPointerException("Entity passed for initialization is null");
        }
        Hibernate.initialize(entity);
        if (entity instanceof HibernateProxy) {
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
