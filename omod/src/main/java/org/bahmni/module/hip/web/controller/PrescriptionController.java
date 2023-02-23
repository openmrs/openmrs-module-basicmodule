package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.PrescriptionBundle;
import org.bahmni.module.hip.web.service.PrescriptionService;
import org.bahmni.module.hip.web.service.ValidationService;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.bahmni.module.hip.web.utils.DateUtils.isDateBetweenDateRange;
import static org.bahmni.module.hip.web.utils.DateUtils.parseDate;
import static org.bahmni.module.hip.web.utils.DateUtils.parseDateTime;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip/prescriptions")
@RestController
public class PrescriptionController extends BaseRestController {
    private final PrescriptionService prescriptionService;
    private final ValidationService validationService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService, ValidationService validationService) {
        this.prescriptionService = prescriptionService;
        this.validationService = validationService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/visit", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getPrescriptionForVisit(
            @RequestParam String patientId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String visitType,
            @RequestParam String visitStartDate
    ) throws ParseException, IOException {
        if (patientId == null || patientId.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noPatientIdProvided());
        if (visitType == null || visitType.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noVisitTypeProvided());
        if (visitStartDate == null || visitStartDate.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noVisitTypeProvided());
        if (!validationService.isValidVisit(visitType))
            return ResponseEntity.badRequest().body(ClientError.invalidVisitType());
        if (!validationService.isValidPatient(patientId))
            return ResponseEntity.badRequest().body(ClientError.invalidPatientId());
        List<PrescriptionBundle> prescriptionBundle = new ArrayList<>();
        if(isDateBetweenDateRange(visitStartDate,fromDate,toDate))
            prescriptionBundle = prescriptionService.getPrescriptions(patientId, visitType, parseDateTime(visitStartDate));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(mapper.writeValueAsString(new BundledPrescriptionResponse(prescriptionBundle)));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/program", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getPrescriptionForProgram(
            @RequestParam String patientId,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam String programName,
            @RequestParam String programEnrollmentId
    ) throws ParseException, IOException {
        programName = URLDecoder.decode(programName, "UTF-8");
        if (patientId == null || patientId.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noPatientIdProvided());
        if (programName == null || programName.isEmpty())
            return ResponseEntity.badRequest().body(ClientError.noProgramNameProvided());
        if (!validationService.isValidProgram(programName))
            return ResponseEntity.badRequest().body(ClientError.invalidProgramName());
        if (!validationService.isValidPatient(patientId))
            return ResponseEntity.badRequest().body(ClientError.invalidPatientId());
        List<PrescriptionBundle> prescriptionBundle =
                prescriptionService.getPrescriptionsForProgram(patientId, new DateRange(parseDate(fromDate), parseDate(toDate)), programName, programEnrollmentId);
         return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(mapper.writeValueAsString(new BundledPrescriptionResponse(prescriptionBundle)));
    }
}
