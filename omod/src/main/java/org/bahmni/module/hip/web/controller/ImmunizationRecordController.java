package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.utils.DateUtil;
import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.model.ImmunizationRecordBundle;
import org.bahmni.module.hip.web.service.ImmunizationRecordService;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;


@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/hip/immunizationRecord")
@RestController
public class ImmunizationRecordController extends BaseRestController {
    private ImmunizationRecordService immunizationRecordService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public ImmunizationRecordController(ImmunizationRecordService immunizationRecordService) {
        this.immunizationRecordService = immunizationRecordService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/visit", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ResponseEntity<?> getImmunizationsForVisit(@RequestParam String patientId,
                                               @RequestParam String visitUuid,
                                               @RequestParam(required = false) String fromDate,
                                               @RequestParam(required = false) String toDate) throws ParseException, IOException {
        if (visitUuid == null || visitUuid.isEmpty()) {
            return ResponseEntity.badRequest().body(ClientError.noVisitTypeProvided());
        }

        Date fromEncounterDate = null, toEncounterDate = null;
        if (!StringUtils.isEmpty(fromDate)) {
            fromEncounterDate = validDate(fromDate);
            if (fromEncounterDate == null) {
                return ResponseEntity.badRequest().body(ClientError.invalidStartDate());
            }
        }

        if (!StringUtils.isEmpty(toDate)) {
            toEncounterDate = validDate(fromDate);
            if (toEncounterDate == null) {
                return ResponseEntity.badRequest().body(ClientError.invalidEndDate());
            }
        }

        List<ImmunizationRecordBundle> immunizationRecordsForVisit
                = immunizationRecordService.getImmunizationRecordsForVisit(patientId, visitUuid, fromEncounterDate, toEncounterDate);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(mapper.writeValueAsString(immunizationRecordsForVisit));
    }

    private Date validDate(String value) {
        try {
            return DateUtil.parseDate(value);
        } catch (RuntimeException re) {
           //log
        }
        return null;
    }
}
