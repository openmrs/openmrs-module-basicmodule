package org.bahmni.module.hip.web.controller;

import junit.framework.TestCase;
import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.service.DiagnosticReportService;
import org.bahmni.module.hip.web.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static java.util.Collections.EMPTY_LIST;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DiagnosticReportController.class, TestConfiguration.class})
@WebAppConfiguration
public class DiagnosticReportControllerTest extends TestCase {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private DiagnosticReportService diagnosticReportService;

    @Autowired
    private ValidationService validationService;

    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    public void shouldReturn200ForVisits() throws Exception {
        when(validationService.isValidVisit("IPD")).thenReturn(true);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(diagnosticReportService.getDiagnosticReportsForVisit(anyString(), anyString(),any()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/visit", RestConstants.VERSION_1))
                .param("visitType", "IPD")
                .param("visitStartDate", "2020-01-01 12:00:00")
                .param("patientId", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400OnInvalidVisitType() throws Exception {
        when(validationService.isValidVisit("OP")).thenReturn(false);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(diagnosticReportService.getDiagnosticReportsForVisit(anyString(), anyString(),any()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/visit", RestConstants.VERSION_1))
                .param("visitType", "OP")
                .param("visitStartDate", "2020-01-01 12:00:00")
                .param("patientId", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn400OnInvalidPatientId() throws Exception {
        when(validationService.isValidVisit("IPD")).thenReturn(true);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745")).thenReturn(false);
        when(diagnosticReportService.getDiagnosticReportsForVisit(anyString(), anyString(),any()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/visit", RestConstants.VERSION_1))
                .param("visitType", "IPD")
                .param("visitStartDate", "2020-01-01 12:00:00")
                .param("patientId", "0f90531a-285c-438b-b265-bb3abb4745")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn500ForMissingFieldForVisit() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/visit", RestConstants.VERSION_1))
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(500, mvcResult.getResponse().getStatus());
    }

    @Test
    public void shouldReturn200ForForPrograms() throws Exception {
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(validationService.isValidProgram("HIV Program")).thenReturn(true);
        when(diagnosticReportService.getDiagnosticReportsForProgram(anyString(), any(), anyString(), anyString()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/program", RestConstants.VERSION_1))
                .param("programName", "HIV Program")
                .param("programEnrollmentId", "123")
                .param("patientId", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400OnInvalidProgram() throws Exception {
        when(validationService.isValidVisit("TB")).thenReturn(false);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(diagnosticReportService.getDiagnosticReportsForProgram(anyString(), any(), anyString(), anyString()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/program", RestConstants.VERSION_1))
                .param("programName", "TB")
                .param("programEnrollmentId", "123")
                .param("patientId", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn500ForMissingFieldForPrograms() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/diagnosticReports/program", RestConstants.VERSION_1))
                .param("programName", "IPD")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(500, mvcResult.getResponse().getStatus());
    }
}