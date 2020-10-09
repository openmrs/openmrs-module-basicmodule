package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.model.PatientCareContext;
import org.bahmni.module.hip.web.service.CareContextService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    ResponseEntity<List<PatientCareContext>> getCareContextForPatient(@RequestParam Integer patientId){
        return ResponseEntity.ok(careContextService.careContextForPatient(patientId));
    }
}
