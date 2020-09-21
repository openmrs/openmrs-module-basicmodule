package org.bahmni.module.hip.web.service;

import lombok.Builder;
import lombok.Getter;
import org.hl7.fhir.r4.model.Organization;
import org.openmrs.VisitType;

@Builder
@Getter
public class OrgContext {
    private Organization organization;
    private String webUrl;

    public Class getCareContextType() {
        //Hardcoded right now. Should also deal with programType, visit or visitType.
        return VisitType.class;
    }
}
