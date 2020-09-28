package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.service.PrescriptionService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@Controller
public class PrescriptionController {
    private PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/prescriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<Object> get(@RequestParam String patientId) {
        // todo: define from and to date and visit type as query params
        List<BundledPrescriptionResponse> bundledPrescriptionResponse =
                prescriptionService.getPrescriptions(patientId, new DateRange(getFromDate(), new Date()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(bundledPrescriptionResponse);
    }

    private Date getFromDate() {
        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.minusDays(60);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
