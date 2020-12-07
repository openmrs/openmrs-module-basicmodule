package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.exception.RequestParameterMissingException;
import org.bahmni.module.hip.web.model.BundledPrescriptionResponse;
import org.bahmni.module.hip.web.model.DateRange;
import org.bahmni.module.hip.web.model.PrescriptionBundle;
import org.bahmni.module.hip.web.service.PrescriptionService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;

import static org.bahmni.module.hip.web.utils.DateUtils.parseDate;

@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip")
@RestController
public class PrescriptionController {
    private final PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/prescriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> get(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String visitType,
            @RequestHeader(value = "Authorization", required = false) String basicAuthorization) throws ParseException {

        if (isValidUser(basicAuthorization)) {
            if (patientId == null || patientId.equals(""))
                throw new RequestParameterMissingException("patientId");

            if (visitType == null || visitType.equals(""))
                throw new RequestParameterMissingException("visitType");
            if (toDate == null || toDate.equals(""))
                throw new RequestParameterMissingException("toDate");
            if (fromDate == null || fromDate.equals(""))
                throw new RequestParameterMissingException("toDate");

            List<PrescriptionBundle> prescriptionBundle =
                    prescriptionService.getPrescriptions(patientId, new DateRange(parseDate(fromDate), parseDate(toDate)), visitType);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(new BundledPrescriptionResponse(prescriptionBundle));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ClientError.unauthorizedUser());
    }

    private boolean isValidUser(String basicAuthorization) {
        if (basicAuthorization != null && basicAuthorization.startsWith("Basic")) {
            String base64Credentials = basicAuthorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            return values[0].equals("superman") && values[1].equals("Admin123");
        }
        return false;
    }
}
