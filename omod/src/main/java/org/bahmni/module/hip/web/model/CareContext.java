package org.bahmni.module.hip.web.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CareContext {
    String careContextReference;
    String careContextType;
}
