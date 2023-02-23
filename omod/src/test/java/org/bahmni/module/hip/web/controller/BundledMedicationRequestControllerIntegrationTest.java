package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.bahmni.module.hip.web.service.ValidationService;
import org.hl7.fhir.r4.model.Bundle;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BundledMedicationRequestController.class, TestConfiguration.class})
@WebAppConfiguration
public class BundledMedicationRequestControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private BundleMedicationRequestService bundledMedicationRequestService;

    @Autowired
    private ValidationService validationService;

    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    public void shouldReturn200OkOnSuccess() throws Exception {
        when(validationService.isValidVisit("OPD")).thenReturn(true);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patientId=0f90531a-285c-438b-b265-bb3abb4745bd" +
                "&visitType=OPD")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400OkOnInvalidVisitType() throws Exception {
        when(validationService.isValidVisit("OP")).thenReturn(false);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patientId=0f90531a-285c-438b-b265-bb3abb4745bd" +
                "&visitType=OP")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(validationService, times(1)).isValidVisit("OP");
    }

    @Test
    public void shouldReturn400OkOnInvalidPatientId() throws Exception {
        when(validationService.isValidVisit("OPD")).thenReturn(true);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745")).thenReturn(false);
        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patientId=0f90531a-285c-438b-b265-bb3abb4745" +
                "&visitType=OPD")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(validationService, times(1)).isValidVisit("OPD");
        verify(validationService, times(1)).isValidPatient("0f90531a-285c-438b-b265-bb3abb4745");

    }

    @Test
    public void shouldReturnHttpBadRequestWhenPatientIdIsMissing() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        MvcResult mvcResult = mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication")
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void shouldReturnHttpInternalServerErrorWhenPatientIdIsEmpty() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        MvcResult mvcResult = mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patient=&visitType='OPD'")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void shouldReturnHttpBadRequestWhenVisitTypeIsEmpty() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        MvcResult mvcResult = mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 +
                "/hip/medication?patient='0f90531a-285c-438b-b265-bb3abb4745bd'&visitType=")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(400, mvcResult.getResponse().getStatus());
    }

    @Test
    public void shouldReturnHttpBadRequestWhenPatientIdIsInvalid() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        MvcResult mvcResult = mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 +
                "/hip/medication?patient='0f90531a-285c-4'&visitType=")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(400, mvcResult.getResponse().getStatus());
    }
}
