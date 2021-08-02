package org.bahmni.module.hip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Builder
@AllArgsConstructor
public class PatientCareContext {
    private String careContextType;
    private String careContextName;
    private Integer careContextReference;

    public String getCareContextName() {
        return careContextName;
    }
}
