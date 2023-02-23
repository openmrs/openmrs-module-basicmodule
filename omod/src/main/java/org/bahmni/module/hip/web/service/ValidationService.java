package org.bahmni.module.hip.web.service;

import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.VisitType;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Qualifier("validationService")
public class ValidationService {
    private final VisitService visitService;
    private final PatientService patientService;
    private final ProgramWorkflowService programWorkflowService;
    private final ExistingPatientService existingPatientService;

    @Autowired
    public ValidationService(VisitService visitService, PatientService patientService, ProgramWorkflowService programWorkflowService, ExistingPatientService existingPatientService) {
        this.patientService = patientService;
        this.visitService = visitService;
        this.programWorkflowService = programWorkflowService;
        this.existingPatientService = existingPatientService;
    }

    public boolean isValidVisit(String visitType) {
        List<VisitType> visitTypes = visitService.getAllVisitTypes();
        for (VisitType vType : visitTypes) {
            if (vType.getName().toLowerCase().equals(visitType.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidPatient(String pid) {
        Patient patient = patientService.getPatientByUuid(pid);
        return patient != null;
    }

    public boolean isValidProgram(String programName) {
        Program program = programWorkflowService.getProgramByName(programName);
        if(program != null)
            return true;
        return false;
    }

    public boolean isValidHealthId(String healthId){
        Patient patient = null;
        try {
            patient = patientService.getPatientByUuid(existingPatientService.getPatientWithHealthId(healthId));
        }
        catch (NullPointerException ignored){

        }
        return patient != null;
    }
}
