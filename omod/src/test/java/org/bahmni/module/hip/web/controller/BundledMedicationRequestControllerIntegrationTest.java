package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.service.BundleMedicationRequestService;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
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

    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    public void shouldReturn200OkOnSuccess() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patientId='0f90531a-285c-438b-b265-bb3abb4745bd'" +
                "&visitType='OPD'")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test(expected = NestedServletException.class)
    public void shouldReturnHttpBadRequestWhenPatientIdIsMissing() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void shouldReturnHttpBadRequestWhenPatientIdIsEmpty() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patient=''")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void shouldReturnHttpBadRequestWhenVisitTypeIsMissing() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 + "/hip/medication?patient=''")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void shouldReturnHttpBadRequestWhenVisitTypeIsEmpty() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 +
                "/hip/medication?patient='0f90531a-285c-438b-b265-bb3abb4745bd'&visitType=''")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NestedServletException.class)
    public void shouldReturnPatientIdRequestParameterIsMandatoryErrorMessage() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        MvcResult mvcResult = mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 +
                "/hip/medication?patient=''")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        assertEquals("{\"errMessage\":\"patientId is mandatory request parameter\"}", content);
    }

    @Test(expected = NestedServletException.class)
    public void shouldReturnVisitTypeRequestParameterIsMandatoryErrorMessage() throws Exception {

        when(bundledMedicationRequestService.bundleMedicationRequestsFor(anyString(), anyString()))
                .thenReturn(new Bundle());

        MvcResult mvcResult = mockMvc.perform(get("/rest/" + RestConstants.VERSION_1 +
                "/hip/medication?patientId='0f90531a-285c-438b-b265-bb3abb4745bd'")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();

        assertEquals("{\"errMessage\":\"visitType is mandatory request parameter\"}", content);
    }
}

@Configuration
class TestConfiguration {
    @Bean
    public BundleMedicationRequestService bundleMedicationRequestService() {
        return mock(BundleMedicationRequestService.class);
    }
}