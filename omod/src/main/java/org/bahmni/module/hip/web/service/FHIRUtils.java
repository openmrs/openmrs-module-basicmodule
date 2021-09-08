package org.bahmni.module.hip.web.service;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FHIRUtils {
    private static Map<String, Enumerations.AdministrativeGender> genderMap = new HashMap<String, Enumerations.AdministrativeGender>() {{
        put("M", Enumerations.AdministrativeGender.MALE);
        put("F", Enumerations.AdministrativeGender.FEMALE);
        put("O", Enumerations.AdministrativeGender.OTHER);
        put("U", Enumerations.AdministrativeGender.UNKNOWN);
    }};

    public static Bundle createBundle(Date forDate, String bundleId, String webURL) {
        Bundle bundle = new Bundle();
        bundle.setId(bundleId);
        bundle.setTimestamp(forDate);

        Identifier identifier = new Identifier();
        identifier.setSystem(Utils.ensureTrailingSlash(webURL.trim()) + "/bundle");
        identifier.setValue(bundleId);
        bundle.setIdentifier(identifier);

        Meta bundleMeta = getMeta(forDate);
        bundle.setMeta(bundleMeta);
        bundle.setType(Bundle.BundleType.DOCUMENT);
        return bundle;
    }

    public static Meta getMeta(Date forDate) {
        Meta meta = new Meta();
        meta.setLastUpdated(forDate);
        meta.setVersionId("1.0"); //TODO
        return meta;
    }

    public static Identifier getIdentifier(String id, String domain, String resType) {
        Identifier identifier = new Identifier();
        identifier.setSystem(Utils.ensureTrailingSlash(domain) + resType);
        identifier.setValue(id);
        return identifier;
    }

    public static CodeableConcept getPrescriptionType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("440545006");
        coding.setDisplay("Prescription record");
        return type;
    }

    public static CodeableConcept getDiagnosticReportType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("721981007");
        coding.setDisplay("Diagnostic Report");
        return type;
    }

    public static CodeableConcept getPatientDocumentType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("371530004");
        coding.setDisplay("Clinical consultation report");
        return type;
    }

    public static CodeableConcept getOPConsultType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("371530004");
        coding.setDisplay("Clinical consultation report");
        return type;
    }

    public static CodeableConcept getChiefComplaintType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("422843007");
        coding.setDisplay("Chief complaint");
        return type;
    }

    public static CodeableConcept getMedicalHistoryType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("422843008"); // don't know
        coding.setDisplay("Medical history");
        return type;
    }

    public static CodeableConcept getPhysicalExaminationType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("425044008"); // don't know
        coding.setDisplay("Physical examination");
        return type;
    }

    public static CodeableConcept getProcedureType() {
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("36969009");
        coding.setDisplay("Procedure");
        return type;
    }

    public static CodeableConcept getOrdersType(){
        CodeableConcept type = new CodeableConcept();
        Coding coding = type.addCoding();
        coding.setSystem(Constants.FHIR_SCT_SYSTEM);
        coding.setCode("721963009");
        coding.setDisplay("Order");
        return type;
    }

    public static void addToBundleEntry(Bundle bundle, Resource resource, boolean useIdPart) {
        String resourceType = resource.getResourceType().toString();
        String id = useIdPart ? resource.getIdElement().getIdPart() : resource.getId();
        bundle.addEntry()
                .setFullUrl(resourceType + "/" + id)
                .setResource(resource);
    }

    public static void addToBundleEntry(Bundle bundle, List<? extends Resource> resources, boolean useIdPart) {
        resources.forEach(resource -> FHIRUtils.addToBundleEntry(bundle, resource, useIdPart));
    }

    public static Organization createOrgInstance(String hfrId, String hfrName, String hfrSystem) {
        Organization organization = new Organization();
        organization.setId(hfrId);
        organization.setName(hfrName);
        Identifier identifier = organization.addIdentifier();
        identifier.setSystem(hfrSystem);
        identifier.setValue(hfrId);
        identifier.setUse(Identifier.IdentifierUse.OFFICIAL);
        return organization;
    }

    public static Reference getReferenceToResource(Resource res) {
        Reference ref = new Reference();
        ref.setResource(res);
        return ref;
    }

    public static String getDisplay(Practitioner author) {
        String prefixAsSingleString = author.getNameFirstRep().getPrefixAsSingleString();
        if ("".equals(prefixAsSingleString)) {
            return author.getNameFirstRep().getText();
        } else {
            return prefixAsSingleString.concat(" ").concat(author.getNameFirstRep().getText());
        }
    }
}
