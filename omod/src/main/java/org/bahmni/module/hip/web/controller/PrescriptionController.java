package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.PrescriptionBundle;
import org.bahmni.module.hip.web.service.PrescriptionService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

import static org.bahmni.module.hip.web.utils.DateUtils.parseDate;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@RestController
public class PrescriptionController {
    private PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/prescriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<BundledPrescriptionResponse> get(
            @RequestParam String patientId,
            @RequestParam String fromDate,
            @RequestParam String toDate
    ) throws ParseException {
        // todo: define from and to date and visit type as query params
        List<PrescriptionBundle> prescriptionBundle =
                prescriptionService.getPrescriptions(patientId, new DateRange(parseDate(fromDate), parseDate(toDate)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new BundledPrescriptionResponse(prescriptionBundle));
    }
}
