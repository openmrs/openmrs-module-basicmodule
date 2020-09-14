package org.bahmni.module.hip.web.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.bahmni.module.hip.web.service.MedicationService;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@Controller
public class MedicationController {

    private MedicationService medicationService;

    @Autowired
    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/medication")
    public List<String> getMedication(@RequestParam String patientId, @RequestParam String visitType) {
        try {
            FhirContext ctx = FhirContext.forR4();
            IParser parser = ctx.newJsonParser();
            // Serialize it
            List<MedicationRequest> medicationRequest = medicationService.getMedication(patientId, visitType);

            return medicationRequest
                    .stream()
                    .map(parser::encodeResourceToString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            List<String> errors = new ArrayList<String>();
            errors.add(e.getMessage());
            return errors;
        }
    }
}
