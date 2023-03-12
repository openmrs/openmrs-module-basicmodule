package org.bahmni.module.hip.web.service;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ImmunizationObsTemplateConfig {

    private static String IMMUNIZATION_PROPERTIES_FILE_NAME = "immunization_config.properties";

    public static enum ImmunizationAttribute {
        VACCINE_CODE("immunization.concept.vaccineCode"),
        OCCURRENCE_DATE("immunization.concept.occurrenceDateTime"),
        MANUFACTURER("immunization.concept.manufacturer"),
        DOSE_NUMBER("immunization.concept.doseNumber"),
        LOT_NUMBER("immunization.concept.lotNumber"),
        EXPIRATION_DATE("immunization.concept.expirationDate"),
        ROOT_CONCEPT("immunization.concept.root");

        private final String configKey;

        ImmunizationAttribute(String configKey) {
            this.configKey = configKey;
        }

        public String getConfigKey() {
            return configKey;
        }
    }

    private HashMap<ImmunizationAttribute, String> configProperties;

    public String getRootConcept() {
        return "form1-root-concept";
    }

    public String getVaccineCode() {
        return null;
    }

    public ImmunizationObsTemplateConfig() {
        configProperties = new HashMap<>();
        configProperties.put(ImmunizationAttribute.VACCINE_CODE, "");
        configProperties.put(ImmunizationAttribute.OCCURRENCE_DATE, "");
        configProperties.put(ImmunizationAttribute.MANUFACTURER, "");
        configProperties.put(ImmunizationAttribute.LOT_NUMBER, "");
        configProperties.put(ImmunizationAttribute.DOSE_NUMBER, "");
        configProperties.put(ImmunizationAttribute.EXPIRATION_DATE, "");
        configProperties.put(ImmunizationAttribute.ROOT_CONCEPT, "");
    }

    public void loadFromFile() {
        String propertyFile = new File(OpenmrsUtil.getApplicationDataDirectory(), IMMUNIZATION_PROPERTIES_FILE_NAME).getAbsolutePath();
        log.info(String.format("Reading  properties from : %s", propertyFile));
        System.out.printf(String.format("Reading  properties from : %s", propertyFile));
        try {
            Properties properties = new Properties(System.getProperties());
            properties.load(new FileInputStream(propertyFile));
            Arrays.stream(ImmunizationAttribute.values()).forEach(conceptType -> {
                configProperties.put(conceptType, (String) properties.get(conceptType.getConfigKey()));
            });
        } catch (IOException e) {
            log.error("Error Occurred while trying to load immunization_config.properties", e);
        }
    }

    public void loadFromProperties(Properties props) {
        Arrays.stream(ImmunizationAttribute.values()).forEach(conceptType -> {
            configProperties.put(conceptType, (String) props.get(conceptType.getConfigKey()));
        });
    }

    public String getConfigProperty(ImmunizationAttribute conceptType) {
        return configProperties.get(conceptType);
    }

    public Map<ImmunizationAttribute, String> getImmunizationAttributeConfigs() {
        return configProperties;
    }
}
