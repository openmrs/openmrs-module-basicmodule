package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.bahmni.module.hip.web.service.ValidationService;
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
    private final BundleMedicationRequestService bundledMedicationRequestService;
    private final ValidationService validationService;

    @Autowired
    public BundledMedicationRequestController(BundleMedicationRequestService bundledMedicationRequestService, ValidationService validationService) {
        this.bundledMedicationRequestService = bundledMedicationRequestService;
        this.validationService = validationService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/medication", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getBundledMedicationRequestFor(@RequestParam(required = false) String patientId,
                                                     @RequestParam(required = false) String visitType) {

        if (patientId == null || patientId.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noPatientIdProvided());
        if (visitType == null || visitType.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noVisitTypeProvided());
        if (!validationService.isValidVisit(visitType))
            return ResponseEntity.badRequest().body(ClientError.invalidVisitType());
        if (!validationService.isValidPatient(patientId))
            return ResponseEntity.badRequest().body(ClientError.invalidPatientId());
        Bundle bundle = bundledMedicationRequestService.bundleMedicationRequestsFor(patientId, visitType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(serializeBundle(bundle));
    }

}