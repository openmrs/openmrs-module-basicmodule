package org.bahmni.module.hip.web.model;

import lombok.Builder;
import lombok.Getter;
import org.hl7.fhir.r4.model.Organization;
import org.openmrs.VisitType;

@Builder
public class OrganizationContext {
    private Organization organization;
    private String webUrl;

    public Class careContextType() {
        //Hardcoded right now. Should also deal with programType, visit or visitType.
        return VisitType.class;
    }

    public String webUrl() {
        return webUrl;
    }
}
