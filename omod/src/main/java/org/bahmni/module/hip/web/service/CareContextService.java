package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.CareContextRepository;
import org.bahmni.module.hip.web.model.CareContext;
import org.openmrs.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CareContextService {
    private final CareContextRepository careContextRepository;

    @Autowired
    public CareContextService(CareContextRepository careContextRepository) {
        this.careContextRepository = careContextRepository;
    }

    CareContext careContextFor(Encounter emrEncounter, Class careContextType) {
        if (careContextType.getName().equals("Visit")) {
            return CareContext.builder()
                    .careContextReference(emrEncounter.getVisit().getUuid())
                    .careContextType("Visit").build();
        } else {
            return CareContext.builder()
                    .careContextReference(emrEncounter.getVisit().getVisitType().getName())
                    .careContextType("VisitType").build();
        }
    }

    public <Type> Type careContextForPatient(String patientUuid) {
        return (Type) careContextRepository.getPatientCareContext(patientUuid);
    }

}
