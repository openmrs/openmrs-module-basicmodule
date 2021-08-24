package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.model.Error;
import org.bahmni.module.hip.web.client.model.ErrorCode;
import org.bahmni.module.hip.web.client.model.ErrorRepresentation;
import org.bahmni.module.hip.web.model.ExistingPatient;
import org.bahmni.module.hip.web.service.ExistingPatientService;
import org.openmrs.Patient;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@RestController
public class PatientController {
    private final ExistingPatientService existingPatientService;

    @Autowired
    public PatientController(ExistingPatientService existingPatientService) {
        this.existingPatientService = existingPatientService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/existingPatients", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getExistingPatients(@RequestParam(required = false) String patientName,
                                          @RequestParam String patientYearOfBirth,
                                          @RequestParam String patientGender,
                                          @RequestParam String phoneNumber) {
        List<Patient> matchingPatients = existingPatientService.getMatchingPatients(phoneNumber);
        matchingPatients.addAll(existingPatientService.getMatchingPatients(patientName,
                Integer.parseInt(patientYearOfBirth), patientGender));
        if (matchingPatients.size() == 0) {
            return ResponseEntity.ok().body(new ErrorRepresentation(new Error(
                    ErrorCode.PATIENT_ID_NOT_FOUND, "No patient found")));
        }
        List<ExistingPatient> existingPatients = existingPatientService.getMatchingPatientDetails(matchingPatients);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(existingPatients);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/existingPatients/{healthId}")
    @ResponseBody
    public ResponseEntity<?> getExistingPatientsWithHealthId(@PathVariable String healthId) {
        String patientUuid = existingPatientService.getPatientWithHealthId(healthId);
        if (patientUuid != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(patientUuid);
        } else {
            return ResponseEntity.ok()
                    .body(new ErrorRepresentation(new Error(ErrorCode.PATIENT_ID_NOT_FOUND, "No patient found")));
        }
    }
}
