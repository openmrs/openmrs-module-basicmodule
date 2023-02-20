package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.client.model.Error;
import org.bahmni.module.hip.web.client.model.ErrorCode;
import org.bahmni.module.hip.web.client.model.ErrorRepresentation;
import org.bahmni.module.hip.web.model.ExistingPatient;
import org.bahmni.module.hip.web.model.Location;
import org.bahmni.module.hip.web.service.ExistingPatientService;
import org.bahmni.module.hip.web.service.ValidationService;
import org.codehaus.jackson.map.ObjectMapper;
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
import org.springframework.web.bind.annotation.CookieValue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@RestController
public class PatientController {
    private final ExistingPatientService existingPatientService;
    private final ValidationService validationService;

    @Autowired
    public PatientController(ExistingPatientService existingPatientService, ValidationService validationService) {
        this.existingPatientService = existingPatientService;
        this.validationService = validationService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/existingPatients", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getExistingPatients(@RequestParam(required = false) String patientName,
                                          @RequestParam String patientYearOfBirth,
                                          @RequestParam String patientGender,
                                          @RequestParam String phoneNumber,
                                          @CookieValue(name = "bahmni.user.location") String location) throws IOException {
        String locationUuid = new ObjectMapper().readValue(location,Location.class).getUuid();
        Set<Patient> matchingPatients = existingPatientService.getMatchingPatients(locationUuid,phoneNumber,patientName,
                Integer.parseInt(patientYearOfBirth), patientGender);
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
        }
        else {
            return ResponseEntity.ok()
                    .body(new ErrorRepresentation(new Error(ErrorCode.PATIENT_ID_NOT_FOUND, "No patient found")));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/existingPatients/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> perform(@RequestParam String healthId, @RequestParam String action) {
        if(!validationService.isValidHealthId(healthId)) {
            return ResponseEntity.ok().body(ClientError.patientIdentifierNotFound());
        }
        existingPatientService.perform(healthId, action);
        return ResponseEntity.ok().body("");
    }

    @RequestMapping(method = RequestMethod.GET, value = "/existingPatients/IdDeactivationStatus/{patientUuid}")
    @ResponseBody
    public ResponseEntity<?> getIdentifierStatus(@PathVariable String patientUuid) {
        boolean isHealthIdVoided = existingPatientService.isHealthIdVoided(patientUuid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(isHealthIdVoided);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/existingPatients/checkHealthId/{patientUuid}")
    @ResponseBody
    public ResponseEntity<?> checkIfHealthIdExists (@PathVariable String patientUuid) {
        boolean isHealthIdExists =  existingPatientService.checkIfHealthIdExists(patientUuid);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(isHealthIdExists);
    }
}
