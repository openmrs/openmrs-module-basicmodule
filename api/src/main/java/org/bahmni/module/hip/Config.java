package org.bahmni.module.hip;

public enum Config {


    PATIENT_DOCUMENTS_PATH("/home/bahmni/document_images/"),
    ABHA_ADDRESS("ABHA Address"),
    LOCATION("Bahmni Clinics"),
    PHONE_NUMBER("phoneNumber"),

    //encounterType
    CONSULTATION("Consultation"),
    PATIENT_DOCUMENT("Patient Document"),
    RADIOLOGY_TYPE( "RADIOLOGY"),
    ORDER_TYPE("Order"),

    //concepts
    DOCUMENT_TYPE("Document"),
    RADIOLOGY_REPORT("Radiology Report"),
    CHIEF_COMPLAINT( "Chief Complaint"),
    PROCEDURE_NOTES( "Procedure Notes"),
    DISCHARGE_SUMMARY( "Discharge Summary"),
    CODED_DIAGNOSIS( "Coded Diagnosis"),
    NON_CODED_DIAGNOSIS( "Non-coded Diagnosis"),
    LAB_REPORT( "LAB_REPORT"),
    RADIOLOGY_ORDER( "Radiology Order"),
    LAB_ORDER( "Lab Order"),


    PROP_HFR_ID("bahmniHip.healthFacilityRegistryId"),
    PROP_HFR_NAME( "bahmniHip.healthFacilityName"),
    PROP_HFR_SYSTEM( "bahmniHip.healthFacilitySystem"),
    PROP_HFR_URL( "bahmniHip.healthFacilityUrl");


    private final String value;

    Config(String val) {
        value = val;
    }

    public String getValue() {
        return System.getenv().getOrDefault(name(),this.value);
    }

}
