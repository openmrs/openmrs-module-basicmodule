package org.bahmni.module.hip;

public enum Config {


    PATIENT_DOCUMENTS_PATH("/home/bahmni/document_images/"),
    LOCATION("Bahmni Clinic"),

    //attribute name
    PHONE_NUMBER("phoneNumber"),

    //identifier type
    ABHA_ADDRESS("ABHA Address"),
    ABHA_NUMBER("ABHA Number"),

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
    IMAGE("Image"),
    PATIENT_VIDEO("Patient Video"),
    CONCEPT_DETAILS_CONCEPT_CLASS("Concept Details"),
    CHIEF_COMPLAINT_CODED("Chief Complaint Coded"),
    SIGN_SYMPTOM_DURATION("Sign/symptom duration"),
    CHIEF_COMPLAINT_DURATION("Chief Complaint Duration"),

    //Physical_Examination_Ignoring_Form_List
    Forms_To_Ignore_In_Physical_Examination("Discharge Summary, Death Note, Delivery Note, Opioid Substitution Therapy - Intake, Opportunistic Infection, " +
            "Safe Abortion, ECG Notes, Operative Notes, USG Notes, Procedure Notes,Triage Reference, History and Examination, Visit Diagnoses, Patient Video"),


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
