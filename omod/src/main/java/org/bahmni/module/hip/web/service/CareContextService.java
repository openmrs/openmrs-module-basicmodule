package org.bahmni.module.hip.web.service;

import org.bahmni.module.hip.api.dao.CareContextRepository;
import org.bahmni.module.hip.api.dao.ExistingPatientDao;
import org.bahmni.module.hip.model.PatientCareContext;
import org.bahmni.module.hip.web.model.CareContext;
import org.bahmni.module.hip.web.model.serializers.NewCareContext;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CareContextService {
    private final CareContextRepository careContextRepository;
    private final PatientService patientService;
    private final ValidationService validationService;
    private final ExistingPatientDao existingPatientDao;

    @Autowired
    public CareContextService(CareContextRepository careContextRepository, PatientService patientService, ValidationService validationService, ExistingPatientDao existingPatientDao) {
        this.careContextRepository = careContextRepository;
        this.patientService = patientService;
        this.validationService = validationService;
        this.existingPatientDao = existingPatientDao;
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

    public NewCareContext newCareContextsForPatient(String patientUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        return new NewCareContext(patient.getGivenName() + (patient.getMiddleName() == null ? " " : patient.getMiddleName()) + patient.getFamilyName(),
                existingPatientDao.getPatientHealthIdWithPatientId(patient.getId()),
                patient.getPatientIdentifier("Patient Identifier").getIdentifier(),
                getCareContexts(patient));
    }

    private List<PatientCareContext> getCareContexts(Patient patient) {
        List<PatientCareContext> patientCareContexts = careContextRepository.getNewPatientCareContext(patient);
        if (patientCareContexts.size() > 1) {
            List<PatientCareContext> result = new ArrayList<>();
            for (PatientCareContext careContext : patientCareContexts) {
                if (!validationService.isValidVisit(careContext.getCareContextName())) result.add(careContext);
            }
            return result;
        }
        return patientCareContexts;
    }
}
