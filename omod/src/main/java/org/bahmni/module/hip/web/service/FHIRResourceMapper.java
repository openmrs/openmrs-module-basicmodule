package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.*;
import org.openmrs.*;
import org.openmrs.module.fhir2.api.translators.MedicationRequestTranslator;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.impl.PractitionerTranslatorProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FHIRResourceMapper {

    private final PatientTranslator patientTranslator;
    private final PractitionerTranslatorProviderImpl practitionerTranslatorProvider;
    private final MedicationRequestTranslator medicationRequestTranslator;

    private static Map<String, String> encounterTypeMap = new HashMap<String, String>() {{
        put("ADMISSION", "IMP,inpatient encounter");
        put("CONSULTATION", "AMB,ambulatory");
        put("DISCHARGE", "IMP,inpatient encounter");
        put("REG", "AMB,ambulatory");
        put("TRANSFER", "IMP,inpatient encounter");
    }};

    private static Map<String, String> visitTypeMap = new HashMap<String, String>() {{
        put("IPD", "IMP,inpatient encounter");
        put("OPD", "AMB,ambulatory");
        put("EMERGENCY", "EMER,emergency");
        put("FIELD", "FLD,field");
    }};

    private static Map<String, String> conceptSourceSystemMap = new HashMap<String, String>() {{
        put("SCT", "http://snomed.info/sct");
        put("ICD-10-WHO", "http://hl7.org/fhir/ValueSet/icd-10");
        put("ATC", "http://www.whocc.no/atc");
        put("EXAMPLE", "http://example.org/codes");
    }};

    @Autowired
    public FHIRResourceMapper(PatientTranslator patientTranslator, PractitionerTranslatorProviderImpl practitionerTranslatorProvider, MedicationRequestTranslator medicationRequestTranslator) {
        this.patientTranslator = patientTranslator;
        this.practitionerTranslatorProvider = practitionerTranslatorProvider;
        this.medicationRequestTranslator = medicationRequestTranslator;
    }

    public static Encounter mapToEncounter(org.openmrs.Encounter emrEncounter) {
        Encounter encounter = new Encounter();
        Period period = new Period();
        Visit visit = emrEncounter.getVisit();
        boolean isOPVisit = visit.getVisitType().getName().equalsIgnoreCase("OPD");
        //TODO: the mapping needs to be fixed
        /**
         * In case of Inpatient visit
         * - the period should be entire hospitalization period, and mentioned through encounter.location.period
         * - and in that case, encounter.participant.period should be specified for each practitioner encounter period
         * In case of outpatient visit
         * - encounter.period ought to be still visit period
         * - while participant.period should be period of the specific encounter
         */
        //TODO fix as per above
        if (isOPVisit) {
            period.setStart(emrEncounter.getEncounterDatetime());
            period.setEnd(emrEncounter.getEncounterDatetime());
        } else {
            period.setStart(visit.getStartDatetime());
            period.setEnd(visit.getStopDatetime());
        }

        encounter.setPeriod(period);
        encounter.setStatus(Encounter.EncounterStatus.FINISHED);
        Coding coding = new Coding();
        coding.setSystem(Constants.FHIR_ENC_CLASS_SYSTEM);

        String encounterClassDetails = getEncounterClass(visit.getVisitType().getName());
        String[] parts = encounterClassDetails.split(",");
        coding.setCode(parts[0]);
        coding.setDisplay(parts[1]);
        encounter.setClass_(coding);
        encounter.setId(emrEncounter.getUuid());

        //TODO - add encounter.location
        return encounter;
    }

    private static String getEncounterClass(String visitType) {
        String encClassDetails = visitTypeMap.get(visitType.toLowerCase());
        if (encClassDetails == null) {
            encClassDetails = visitTypeMap.get("OPD");
        }
        return encClassDetails;
    }

    public Patient mapToPatient(org.openmrs.Patient emrPatient) {
        return patientTranslator.toFhirResource(emrPatient);
    }

    public Practitioner mapToPractitioner(EncounterProvider encounterProvider) {
        return practitionerTranslatorProvider.toFhirResource(encounterProvider.getProvider());
    }

    public MedicationRequest mapToMedicationRequest(DrugOrder order) {
        return medicationRequestTranslator.toFhirResource(order);
    }

    public static Medication mapToMedication(DrugOrder order) {
        if (!Utils.isBlank(order.getDrugNonCoded())) {
            return null;
        }
        Medication medication = new Medication();
        Drug drug = order.getDrug();
        String drugId, drugName;
        List<Coding> codings;
        if (drug == null) {
            drugId = order.getConcept().getUuid();
            drugName = order.getConcept().getName().getName();
            codings = order.getConcept().getConceptMappings().stream().map(cm -> {
                return mapToCodeableConcept(cm.getConceptReferenceTerm());
            }).collect(Collectors.toList());
        } else {
            drugId = drug.getUuid();
            drugName = drug.getDisplayName();
            codings = mapToCodeableConcept(drug.getDrugReferenceMaps());
        }
        medication.setId(drugId);
        CodeableConcept concept = new CodeableConcept();
        concept.setText(drugName);
        if (codings != null && !codings.isEmpty()) {
            concept.setCoding(codings);
        }
        medication.setCode(concept);
        return medication;
    }

    private static Coding mapToCodeableConcept(ConceptReferenceTerm crt) {
        Coding coding = new Coding();
        coding.setSystem(getCodingSystem(crt.getConceptSource()));
        coding.setCode(crt.getCode());
        coding.setDisplay(crt.getName());
        return coding;
    }

    private static List<Coding> mapToCodeableConcept(Set<DrugReferenceMap> drugReferenceMaps) {
        return drugReferenceMaps.stream().map(drm -> {
            Coding coding = new Coding();
            coding.setSystem(getCodingSystem(drm.getConceptReferenceTerm().getConceptSource()));
            coding.setCode(drm.getConceptReferenceTerm().getCode());
            coding.setDisplay(drm.getConceptReferenceTerm().getName());
            return coding;
        }).collect(Collectors.toList());
    }

    private static String getCodingSystem(ConceptSource conceptSource) {
        //TODO
        return conceptSource.getHl7Code();
    }
}
