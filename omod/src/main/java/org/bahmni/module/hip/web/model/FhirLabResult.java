package org.bahmni.module.hip.web.model;

import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.web.service.FHIRResourceMapper;
import org.bahmni.module.hip.web.service.FHIRUtils;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


public class FhirLabResult {

    private final Patient patient;
    private final Encounter encounter;
    private final Date visitTime;
    private final List<DiagnosticReport>  report;
    private final List<Observation> results;
    private final List<Practitioner> practitioners;

    public FhirLabResult(Patient patient, String panelName, Encounter encounter, Date visitTime, List<DiagnosticReport> report, List<Observation> results, List<Practitioner> practitioners) {
        this.patient = patient;
        this.encounter = encounter;
        this.visitTime = visitTime;
        this.report = report;
        this.results = results;
        this.practitioners = practitioners;
    }

    public Bundle bundleLabResults (String webUrl, FHIRResourceMapper fhirResourceMapper) {
        String bundleID = String.format("LR-%s", encounter.getId());

        Bundle bundle = FHIRUtils.createBundle(visitTime, bundleID, webUrl);

        FHIRUtils.addToBundleEntry(bundle, compositionFrom(webUrl), false);

        FHIRUtils.addToBundleEntry(bundle, encounter, false);
        FHIRUtils.addToBundleEntry(bundle, practitioners, false);
        FHIRUtils.addToBundleEntry(bundle, patient, false);
        FHIRUtils.addToBundleEntry(bundle, report, false);
        FHIRUtils.addToBundleEntry(bundle, results, false);
        return bundle;

    }

    public static FhirLabResult fromOpenMrsLabResults(OpenMrsLabResults labresult, FHIRResourceMapper fhirResourceMapper) {
        List<DiagnosticReport> reportList = new ArrayList<>();
        List<Practitioner> practitioners = labresult.getEncounterProviders().stream().map(fhirResourceMapper::mapToPractitioner).collect(Collectors.toList());

        Patient patient = fhirResourceMapper.mapToPatient(labresult.getPatient());

        for(Map.Entry<Obs, List<LabOrderResult>> report : labresult.getLabOrderResults().entrySet()) {
            DiagnosticReport reports = new DiagnosticReport();
            LabOrderResult firstresult = (report.getValue() != null && report.getValue().size() != 0) ? report.getValue().get(0) : new LabOrderResult();
            String testName = report.getKey().getObsGroup().getConcept().getName().getName();
            reports.setCode(new CodeableConcept().setText(testName).addCoding(new Coding().setDisplay(testName)));
            try {
                reports.setPresentedForm(getAttachments(report.getKey(),testName));
            } catch (IOException e) {
                e.printStackTrace();
            }

            reports.setId(firstresult.getOrderUuid() != null ? firstresult.getOrderUuid() : UUID.randomUUID().toString());
            reports.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
            reports.setSubject(FHIRUtils.getReferenceToResource(patient));
            reports.setResultsInterpreter(practitioners.stream().map(FHIRUtils::getReferenceToResource).collect(Collectors.toList()));

            List<Observation> results = new ArrayList<>();

            if(report.getValue() != null) report.getValue().stream().forEach(result -> FhirLabResult.mapToObsFromLabResult(result, patient, reports, results));
            reportList.add(reports);

        }

        FhirLabResult fhirLabResult = new FhirLabResult(fhirResourceMapper.mapToPatient( labresult.getPatient()), null,
                fhirResourceMapper.mapToEncounter( labresult.getEncounter() ),
                labresult.getEncounter().getVisit().getStartDatetime(), reportList, new ArrayList<>(), practitioners);

        return fhirLabResult;
    }

    private static void mapToObsFromLabResult(LabOrderResult result, Patient patient, DiagnosticReport report, List<Observation> observations) {

        Observation obs = new Observation();

        obs.setId(result.getTestUuid());
        obs.setCode(new CodeableConcept().setText( result.getTestName( )));
        try {
            float f = result.getResult()!=null? Float.parseFloat(result.getResult()): (float) 0;
            obs.setValue(new Quantity().setValue(f).setUnit(result.getTestUnitOfMeasurement()));
        } catch (NumberFormatException | NullPointerException ex) {
            obs.setValue(new StringType().setValue(result.getResult()));
        }
        obs.setStatus(Observation.ObservationStatus.FINAL);

        report.addResult(FHIRUtils.getReferenceToResource(obs));

        observations.add(obs);
    }

    private Composition compositionFrom(String webURL) {
        Composition composition = initializeComposition(visitTime, webURL);
        Composition.SectionComponent compositionSection = composition.addSection();
        Reference patientReference = FHIRUtils.getReferenceToResource(patient);

        practitioners
                .forEach(practitioner -> composition
                        .addAuthor().setResource(practitioner).setDisplay(FHIRUtils.getDisplay(practitioner)));
        composition
                .setEncounter(FHIRUtils.getReferenceToResource(encounter))
                .setSubject(patientReference);

        compositionSection
                .setTitle("Diagnostic Report")
                .setCode(FHIRUtils.getDiagnosticReportType());

        report.stream()
                .map(FHIRUtils::getReferenceToResource)
                .forEach(compositionSection::addEntry);


        return composition;
    }

    private Composition initializeComposition(Date visitTimestamp, String webURL) {
        Composition composition = new Composition();

        composition.setId(UUID.randomUUID().toString());
        composition.setDate(visitTimestamp);
        composition.setIdentifier(FHIRUtils.getIdentifier(composition.getId(), webURL, "document"));
        composition.setStatus(Composition.CompositionStatus.FINAL);
        composition.setType(FHIRUtils.getDiagnosticReportType());
        composition.setTitle("Diagnostic Report");
        return composition;
    }

    private static List<Attachment> getAttachments(Obs obs,String testNmae) throws IOException {
        List<Attachment> attachments = new ArrayList<>();

        Attachment attachment = new Attachment();
        attachment.setContentType(FHIRUtils.getTypeOfTheObsDocument(obs.getValueText()));
        byte[] fileContent = Files.readAllBytes(new File(Config.PATIENT_DOCUMENTS_PATH.getValue() + obs.getValueText()).toPath());
        attachment.setData(fileContent);
        attachment.setTitle("LAB REPORT : " + testNmae);
        attachments.add(attachment);

        return attachments;
    }
}
