package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.web.model.ImmunizationRecordBundle;
import org.bahmni.module.hip.web.model.OrganizationContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.*;
import org.openmrs.Encounter;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils;

import java.util.*;
import java.util.stream.Collectors;

public class FhirImmunizationRecordBundleBuilder {
    private final FHIRResourceMapper fhirResourceMapper;
    private final EncounterTranslator<Encounter> encounterTranslator;
    private final OrganizationContext orgContext;
    private final Map<ImmunizationObsTemplateConfig.ImmunizationAttribute, Concept> immunizationAttributeConceptMap;
    private final ConceptTranslator conceptTranslator;

    public FhirImmunizationRecordBundleBuilder(FHIRResourceMapper fhirResourceMapper,
                                               ConceptTranslator conceptTranslator,
                                               EncounterTranslator<Encounter> encounterTranslator,
                                               OrganizationContext orgContext,
                                               Map<ImmunizationObsTemplateConfig.ImmunizationAttribute, Concept> immunizationAttributeConceptMap) {
        this.fhirResourceMapper = fhirResourceMapper;
        this.encounterTranslator = encounterTranslator;
        this.orgContext = orgContext;
        this.immunizationAttributeConceptMap = immunizationAttributeConceptMap;
        this.conceptTranslator = conceptTranslator;
    }

    public List<ImmunizationRecordBundle> build(Encounter encounter) {
        return encounter.getObsAtTopLevel(false)
                .stream()
                .filter(topLevelObs -> isApplicable(topLevelObs.getConcept()))
                .map(obs -> buildImmunizationBundle(obs, encounter))
                .collect(Collectors.toList());
    }

    private boolean isApplicable(Concept rootConcept) {
        Concept obsRootConcept = immunizationAttributeConceptMap.get(ImmunizationObsTemplateConfig.ImmunizationAttribute.ROOT_CONCEPT);
        return obsRootConcept != null && obsRootConcept.getUuid().equals(rootConcept.getUuid());
    }

    private ImmunizationRecordBundle buildImmunizationBundle(Obs obs, Encounter encounter) {
        Immunization incident = immunizationFrom(obs);
        Patient patient = fhirResourceMapper.mapToPatient(encounter.getPatient());
        incident.setPatient(FHIRUtils.getReferenceToResource(patient));

        String bundleId = String.format("IR-%d-%d", obs.getEncounter().getId(), obs.getId());
        Bundle bundle = FHIRUtils.createBundle(obs.getObsDatetime(), bundleId, orgContext.webUrl());

        Composition document = compositionFrom(encounter.getEncounterDatetime(), UUID.randomUUID().toString());
        FHIRUtils.addToBundleEntry(bundle, document, false);
        Composition.SectionComponent section = document.addSection();
        section.setTitle("# Immunization Record");
        section.setCode(FHIRUtils.getImmunizationRecordType());
        section.addEntry(FHIRUtils.getReferenceToResource(incident));

        FHIRUtils.addToBundleEntry(bundle, patient, false); //add patient
        org.hl7.fhir.r4.model.Encounter immunizationEncounter = encounterTranslator.toFhirResource(encounter);
        //FHIRUtils.addToBundleEntry(bundle, fhirResourceMapper.mapToEncounter(encounter), false);
        FHIRUtils.addToBundleEntry(bundle, immunizationEncounter, false);
        //immunization.setEncounter(this.visitReferenceTranslator.toFhirResource(openmrsImmunization.getEncounter().getVisit()));
        incident.setEncounter(FHIRUtils.getReferenceToResource(immunizationEncounter));
        FHIRUtils.addToBundleEntry(bundle, incident, false);
        List<Practitioner> practitioners = practitionersFrom(encounter.getEncounterProviders());
        FHIRUtils.addToBundleEntry(bundle, practitioners, false);
        incident.setPerformer(Collections.singletonList(
                new Immunization.ImmunizationPerformerComponent(FHIRUtils.getReferenceToResource(practitioners.get(0)))
        ));
        return new ImmunizationRecordBundle(bundle);

    }

    private Immunization immunizationFrom(Obs openmrsImmunization) {
        Immunization immunization = new Immunization();
        immunization.setId(openmrsImmunization.getUuid());
        immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

        if (openmrsImmunization.isObsGrouping() && openmrsImmunization.hasGroupMembers()) {
            openmrsImmunization.getGroupMembers().forEach(member -> {
                Concept memberConcept = member.getConcept();

                if (conceptMatchesForAttribute(memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute.VACCINE_CODE)) {
                    immunization.setVaccineCode(conceptTranslator.toFhirResource(member.getValueCoded()));
                }

                if (conceptMatchesForAttribute(memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute.OCCURRENCE_DATE)) {
                    Date valueDatetime = member.getValueDatetime();
                    if (valueDatetime != null) {
                        immunization.setOccurrence(new DateTimeType(valueDatetime));
                    }
                }

                if (conceptMatchesForAttribute(memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute.DOSE_NUMBER)) {
                    Double valueNumeric = member.getValueNumeric();
                    if (valueNumeric != null) {
                        immunization.addProtocolApplied(new Immunization.ImmunizationProtocolAppliedComponent(new PositiveIntType(valueNumeric.intValue())));
                    }
                }

                if (conceptMatchesForAttribute(memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute.MANUFACTURER)) {
                    String manufacturer = member.getValueText();
                    if (manufacturer != null) {
                        immunization.setManufacturer((new Reference()).setDisplay(manufacturer));
                    }
                }

                if (conceptMatchesForAttribute(memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute.LOT_NUMBER)) {
                    String lotNumber = member.getValueAsString(Locale.ENGLISH);
                    if (lotNumber != null) {
                        immunization.setLotNumber(lotNumber);
                    }
                }

                if (conceptMatchesForAttribute(memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute.EXPIRATION_DATE)) {
                    Date valueDatetime = member.getValueDatetime();
                    if (valueDatetime != null) {
                        immunization.setExpirationDate(valueDatetime);
                    }
                }
            });
        }

        immunization.getMeta().setLastUpdated(FhirTranslatorUtils.getLastUpdated(openmrsImmunization));

        return immunization;
    }

    private boolean conceptMatchesForAttribute(Concept memberConcept, ImmunizationObsTemplateConfig.ImmunizationAttribute immunizationAttribute) {
        Concept mappedConcept = immunizationAttributeConceptMap.get(immunizationAttribute);
        return mappedConcept != null && memberConcept.getUuid().equals(mappedConcept.getUuid());
    }


//    private Map<String, Obs> getObsMembersMap(Obs openmrsImmunization) {
//        if (openmrsImmunization != null && openmrsImmunization.isObsGrouping() && openmrsImmunization.hasGroupMembers()) {
//            openmrsImmunization.getGroupMembers().stream().
//
//            Map<Concept, String> concepts = (Map) ImmunizationTranslatorImpl.IMMUNIZATION_CONCEPTS.stream().collect(Collectors.toMap(this::concept, (refTerm) -> {
//                return refTerm;
//            }));
//            return (Map)obs.getGroupMembers().stream().filter((o) -> {
//                return concepts.containsKey(o.getConcept());
//            }).collect(Collectors.toMap((o) -> {
//                return (String)concepts.get(o.getConcept());
//            }, (o) -> {
//                return o;
//            }));
//            return new HashMap<>();
//        } else {
//            return Collections.emptyMap();
//        }
//    }

    private Composition compositionFrom(Date encounterDatetime, String documentId) {
        Composition document = new Composition();
        document.setId(documentId);
        document.setDate(encounterDatetime);
        document.setIdentifier(FHIRUtils.getIdentifier(document.getId(), orgContext.webUrl(), "document"));
        document.setStatus(Composition.CompositionStatus.FINAL);
        document.setType(FHIRUtils.getImmunizationRecordType());
        document.setTitle("Immunization Incident Record");
        return document;
    }

    private List<Practitioner> practitionersFrom(Set<EncounterProvider> encounterProviders) {
        return encounterProviders
                .stream()
                .map(fhirResourceMapper::mapToPractitioner)
                .collect(Collectors.toList());
    }
}
