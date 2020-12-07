package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.exception.RequestParameterMissingException;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.hibernate.validator.constraints.NotEmpty;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.bahmni.module.hip.web.model.serializers.BundleSerializer.serializeBundle;

@Validated
@RestController
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
public class BundledMedicationRequestController {
    private BundleMedicationRequestService bundledMedicationRequestService;

    @Autowired
    public BundledMedicationRequestController(BundleMedicationRequestService bundledMedicationRequestService) {
        this.bundledMedicationRequestService = bundledMedicationRequestService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/medication", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getBundledMedicationRequestFor(@NotNull @NotEmpty @RequestParam(required = false) String patientId,
                                                     @NotNull @NotEmpty @RequestParam(required = false) String visitType,
                                                     @RequestHeader(value = "Authorization", required = false) String basicAuthorization) {
        if (isValidUser(basicAuthorization)) {
            if (patientId == null || patientId.equals(""))
                throw new RequestParameterMissingException("patientId");

            if (visitType == null || visitType.equals(""))
                throw new RequestParameterMissingException("visitType");

            Bundle bundle = bundledMedicationRequestService.bundleMedicationRequestsFor(patientId, visitType);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(serializeBundle(bundle));
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
