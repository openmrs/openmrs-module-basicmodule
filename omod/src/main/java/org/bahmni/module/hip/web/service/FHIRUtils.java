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
import java.util.stream.Collectors;

public class FHIRUtils {
    private static Map<String, Enumerations.AdministrativeGender> genderMap = new HashMap<String, Enumerations.AdministrativeGender>() {{
        put("M", Enumerations.AdministrativeGender.MALE);
        put("F", Enumerations.AdministrativeGender.FEMALE);
        put("O", Enumerations.AdministrativeGender.OTHER);
        put("U", Enumerations.AdministrativeGender.UNKNOWN);
    }};

    public static Bundle createBundle(Date forDate, String bundleId, OrgContext hipContext) {
        Bundle bundle = new Bundle();
        bundle.setId(bundleId);
        bundle.setTimestamp(forDate);

        Identifier identifier = new Identifier();
        identifier.setSystem(Utils.ensureTrailingSlash(hipContext.getWebUrl().trim()) + "/bundle");
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

    public static void addToBundleEntry(Bundle bundle, Resource resource, boolean useIdPart) {
        String resourceType = resource.getResourceType().toString();
        String id = useIdPart ? resource.getIdElement().getIdPart() : resource.getId();
        bundle.addEntry()
                .setFullUrl(resourceType + "/" + id)
                .setResource(resource);
    }

    private static String getHospitalSystemForType(String hospitalDomain, String type) {
        return String.format(Constants.HOSPITAL_SYSTEM, hospitalDomain, type);
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

    public static List<Identifier> getEmrPatientIdentifiers(org.openmrs.Patient emrPatient) {
        return emrPatient.getIdentifiers().stream().map(id -> {
            Identifier identifier = new Identifier();
            identifier.setValue(id.getIdentifier());
            return identifier;
        }).collect(Collectors.toList());
    }

    public static Enumerations.AdministrativeGender getGender(String gender) {
        Enumerations.AdministrativeGender patientGender = genderMap.get(gender.toUpperCase());
        return patientGender != null ? patientGender : Enumerations.AdministrativeGender.UNKNOWN;
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
