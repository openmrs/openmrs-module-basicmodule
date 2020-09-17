package org.bahmni.module.hip.web.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.hl7.fhir.r4.model.Bundle;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@Controller
public class BundledMedicationRequestController {

    private BundleMedicationRequestService bundleMedicationRequestService;

    @Autowired
    public BundledMedicationRequestController(BundleMedicationRequestService bundleMedicationRequestService) {
        this.bundleMedicationRequestService = bundleMedicationRequestService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/medication", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<String> getBundledMedicationRequestFor(@RequestParam String patientId, @RequestParam String visitType) {
        try {

            Bundle bundle = bundleMedicationRequestService.bundleMedicationRequestsFor(patientId, visitType);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(serializeBundle(bundle));

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String serializeBundle(Bundle bundle) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        return parser.encodeResourceToString(bundle);
    }
}