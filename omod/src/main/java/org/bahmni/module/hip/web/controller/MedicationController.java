package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.service.MedicationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String getMedication(@RequestParam String patientId, @RequestParam String visitType) {
        return medicationService.getMedication(patientId, visitType);
    }
}
