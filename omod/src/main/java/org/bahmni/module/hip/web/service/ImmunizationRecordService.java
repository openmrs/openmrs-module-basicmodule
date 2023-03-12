package org.bahmni.module.hip.web.service;

import lombok.extern.slf4j.Slf4j;
import org.bahmni.module.hip.web.model.ImmunizationRecordBundle;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.EncounterTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImmunizationRecordService {

    private final ImmunizationObsTemplateConfig immunizationObsTemplateConfig;
    private final VisitService visitService;
    private ConceptService conceptService;
    private OrganizationContextService organizationContextService;
    private FHIRResourceMapper fhirResourceMapper;
    private ConceptTranslator conceptTranslator;
    private EncounterTranslator<Encounter> encounterTranslator;

    @Autowired
    public ImmunizationRecordService(VisitService visitService, ConceptService conceptService,
                                     OrganizationContextService organizationContextService,
                                     FHIRResourceMapper fhirResourceMapper,
                                     ConceptTranslator conceptTranslator,
                                     EncounterTranslator<Encounter> encounterTranslator) {
        this.visitService = visitService;
        this.conceptService = conceptService;
        this.organizationContextService = organizationContextService;
        this.fhirResourceMapper = fhirResourceMapper;
        this.conceptTranslator = conceptTranslator;
        this.encounterTranslator = encounterTranslator;
        this.immunizationObsTemplateConfig = loadImmunizationObsTemplateConfig();
    }

    public List<ImmunizationRecordBundle> getImmunizationRecordsForVisit(String patientUuid, String visitUuid, Date startDate, Date endDate) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        if (!visit.getPatient().getUuid().equals(patientUuid.trim())) {
            //should log. throw error?
            return Collections.emptyList();
        }
        if (!isImmunizationObsTemplateConfigured()) {
           //no form template configured
            return Collections.emptyList();
        }

        Map<ImmunizationObsTemplateConfig.ImmunizationAttribute, Concept> immunizationAttributeConceptMap =
                immunizationObsTemplateConfig.getImmunizationAttributeConfigs().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> identifyConcept(e)));

        FhirImmunizationRecordBundleBuilder immunizationTransformer =
                new FhirImmunizationRecordBundleBuilder(fhirResourceMapper,
                        conceptTranslator, encounterTranslator,
                        organizationContextService.buildContext(),
                        immunizationAttributeConceptMap);

        return visit.getEncounters().stream()
                .filter(e -> startDate == null || e.getEncounterDatetime().after(startDate))
                .map(immunizationTransformer::build)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Concept identifyConcept(Map.Entry<ImmunizationObsTemplateConfig.ImmunizationAttribute, String> entry) {
        return conceptService.getConceptByUuid(entry.getValue());
    }


    private boolean isImmunizationObsTemplateConfigured() {
        return !StringUtils.isEmpty(immunizationObsTemplateConfig.getRootConcept());
    }

    private ImmunizationObsTemplateConfig loadImmunizationObsTemplateConfig() {
        ImmunizationObsTemplateConfig config = new ImmunizationObsTemplateConfig();
        Properties properties = new Properties();
        properties.put("immunization.concept.vaccineCode", "984AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.put("immunization.concept.occurrenceDateTime", "1410AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.put("immunization.concept.manufacturer", "1419AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.put("immunization.concept.doseNumber", "1418AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.put("immunization.concept.lotNumber", "1420AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.put("immunization.concept.expirationDate", "165907AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.put("immunization.concept.root", "1421AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        //config.loadFromProperties(properties);
        config.loadFromFile();
        return config;
    }


}
