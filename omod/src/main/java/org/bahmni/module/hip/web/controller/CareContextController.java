package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.service.CareContextService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@RestController
public class CareContextController extends BaseRestController {
    private final CareContextService careContextService;

    @Autowired
    public CareContextController(CareContextService careContextService) {
        this.careContextService = careContextService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/careContext", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getCareContextForPatient(@RequestParam(required = false) String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ClientError.noPatientIdProvided());
        }
        if (!careContextService.isValid(patientId)) {
            return ResponseEntity.badRequest().body(ClientError.invalidPatientId());
        }

        Object careContextForPatient = careContextService.careContextForPatient(Integer.parseInt(patientId));
        if (careContextForPatient.equals(false)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ClientError.noPatientFound());
        }

        return ResponseEntity.ok(careContextForPatient);
    }
}
