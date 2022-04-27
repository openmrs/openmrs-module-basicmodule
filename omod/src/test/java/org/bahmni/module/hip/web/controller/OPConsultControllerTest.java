package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.service.OPConsultService;
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
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {OPConsultController.class, TestConfiguration.class})
@WebAppConfiguration
public class OPConsultControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private OPConsultService opConsultService;

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
        when(opConsultService.getOpConsultsForVisit(anyString(), any(), anyString(),any()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/opConsults/visit", RestConstants.VERSION_1))
                .param("visitType", "IPD")
                .param("visitStartDate", "2020-01-01")
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
        when(opConsultService.getOpConsultsForVisit(anyString(), any(), anyString(),any()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/opConsults/visit", RestConstants.VERSION_1))
                .param("visitType", "OP")
                .param("visitStartDate", "2020-01-01")
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
        when(opConsultService.getOpConsultsForVisit(anyString(), any(), anyString(),any()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/opConsults/visit", RestConstants.VERSION_1))
                .param("visitType", "IPD")
                .param("visitStartDate", "2020-01-01")
                .param("patientId", "0f90531a-285c-438b-b265-bb3abb4745")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn500ForMissingFieldForVisit() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/opConsults/visit", RestConstants.VERSION_1))
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(500, mvcResult.getResponse().getStatus());
    }
}