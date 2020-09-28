package org.bahmni.module.hip.web.service;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.*;
import org.openmrs.*;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FHIRResourceMapper {

    private final PatientTranslator patientTranslator;

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
    public FHIRResourceMapper(PatientTranslator patientTranslator) {
        this.patientTranslator = patientTranslator;
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

    private static HumanName mapToHumanName(PersonName personName) {
        HumanName humanName = new HumanName();
        humanName.setFamily(personName.getFamilyName());
        humanName.addGiven(personName.getGivenName());
        humanName.setText(personName.getFullName());
        return humanName;
    }

    public static Practitioner mapToPractitioner(EncounterProvider encounterProvider) {
        Practitioner practitioner = new Practitioner();
        Provider provider = encounterProvider.getProvider();
        practitioner.setId(provider.getIdentifier());
        List<ProviderAttribute> attributes = provider.getAttributes().stream().filter(
                providerAttribute -> providerAttribute.getAttributeType().getName().equalsIgnoreCase("prefix"))
                .collect(Collectors.toList());
        List<StringType> prefixes = attributes.stream().map(p -> new StringType(p.getValue().toString())).collect(Collectors.toList());
        HumanName humanName = mapToHumanName(provider.getPerson().getPersonName());
        humanName.setPrefix(prefixes);
        practitioner.setName(Collections.singletonList(humanName));
        //TODO map identifier
        return practitioner;
    }

    public static MedicationRequest mapToMedicationRequest(DrugOrder order,
                                                           Reference patientRef,
                                                           IBaseResource author,
                                                           Medication medication) {
        MedicationRequest medReq = new MedicationRequest();
        medReq.setId(order.getOrderId().toString());

        medReq.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        medReq.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);

        Reference authorRef = new Reference();
        authorRef.setResource(author);
        medReq.setRequester(authorRef);
        medReq.setAuthoredOn(order.getDateCreated());
        medReq.setSubject(patientRef);

        if (medication == null) {
            CodeableConcept medCodeableConcept = new CodeableConcept();
            medCodeableConcept.setText(order.getDrugNonCoded());
            medReq.setMedication(medCodeableConcept);
        } else {
            Reference medicationRef = FHIRUtils.getReferenceToResource(medication);
            medicationRef.setResource(medication);
            medReq.setMedication(medicationRef);
        }

        medReq.addDosageInstruction().setText(getDosingInstruction(order));
        if (!Utils.isBlank(order.getCommentToFulfiller())) {
            //TODO - should be in dispense instruction
            medReq.addNote().setText(order.getCommentToFulfiller());
        }

        //NOTE: Openmrs does not have means to adding specific reason but we can potentially get something from
        //dosage instruction as text
        //medReq.setReasonReference(some condition);
        return medReq;
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


    private static String getDosingInstruction(DrugOrder order) {
        boolean bahmniSystem = order.getDosingType().getName().equals("org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions");
        if (bahmniSystem) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(order.getDosingInstructions());
                return jsonNode.get("instructions").asText();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }
        return order.getDosingInstructions();
    }
}
