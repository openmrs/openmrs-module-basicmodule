package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.service.CareContextService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@RestController
public class CareContextController {
    private final CareContextService careContextService;

    @Autowired
    public CareContextController(CareContextService careContextService) {
        this.careContextService = careContextService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/careContext", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getCareContextForPatient(@RequestParam(required = false) String patientId,
                                               @RequestHeader(value = "Authorization", required = false) String basicAuthorization) {
        if (isValidUser(basicAuthorization)) {

            if (patientId == null || patientId.equals("") || patientId.equals(" ")) {
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ClientError.unauthorizedUser());
    }

    private boolean isValidUser(String basicAuthorization) {
        if (basicAuthorization != null && basicAuthorization.startsWith("Basic")) {
            String base64Credentials = basicAuthorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            return values[0].equals("superman") && values[1].equals("Admin123");
        }
        return false;
    }
}
