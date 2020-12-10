package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.exception.RequestParameterMissingException;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.bahmni.module.hip.web.model.serializers.BundleSerializer.serializeBundle;

@Validated
@RestController
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
public class BundledMedicationRequestController extends BaseRestController {
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

}