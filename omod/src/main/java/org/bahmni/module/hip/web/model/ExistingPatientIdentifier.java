package org.bahmni.module.hip.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ExistingPatientIdentifier {
    String patientUuid;
    Boolean voided;
}
