package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.exception.RequestParameterMissingException;
import org.bahmni.module.hip.web.model.ErrorResponse;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    ResponseEntity<String> getBundledMedicationRequestFor(@RequestParam(required = false) String patientId,
                                                          @RequestParam(required = false) String visitType) {

        if (patientId == null || patientId.equals("''"))
            throw new RequestParameterMissingException("patientId");

        if (visitType == null || visitType.equals("''"))
            throw new RequestParameterMissingException("visitType");

        Bundle bundle = bundledMedicationRequestService.bundleMedicationRequestsFor(patientId, visitType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(serializeBundle(bundle));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RequestParameterMissingException.class)
    public @ResponseBody
    ErrorResponse missingRequestParameter(Exception ex) {
        return new ErrorResponse(ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalArgumentException(Exception ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public String genericException(Exception ex) {
        return "Something went wrong!!";
    }

}