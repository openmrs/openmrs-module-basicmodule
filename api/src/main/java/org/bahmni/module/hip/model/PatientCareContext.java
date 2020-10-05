package org.bahmni.module.hip.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PatientCareContext {
    private String careContextType;
    private String careContextName;
    private Integer careContextReference;
}
