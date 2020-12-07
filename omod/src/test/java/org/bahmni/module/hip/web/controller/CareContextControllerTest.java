package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.model.PatientCareContext;
import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.service.CareContextService;
import org.codehaus.jackson.map.ObjectMapper;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedCheckedException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static java.util.Collections.EMPTY_LIST;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {CareContextController.class, TestConfiguration.class})
@WebAppConfiguration
public class CareContextControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private CareContextService careContextService;

    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    public void shouldReturn200OkWhenPatientIdIsGiven() throws Exception {
        List<PatientCareContext> patientCareContextList = new ArrayList<>();
        patientCareContextList.add(PatientCareContext.builder()
                .careContextName("TB Program")
                .careContextType("PROGRAM")
                .careContextReference(4)
                .build());
        when(careContextService.isValid(anyString())).thenReturn(true);
        when(careContextService.careContextForPatient(anyInt()))
                .thenReturn(patientCareContextList);

        mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientId", "72")
                .header("authorization", "Basic c3VwZXJtYW46QWRtaW4xMjM=")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400BadRequestWhenPatientIdContainsCharacters() throws Exception {
        List<PatientCareContext> patientCareContextList = new ArrayList<>();
        patientCareContextList.add(PatientCareContext.builder()
                .careContextName("TB Program")
                .careContextType("PROGRAM")
                .careContextReference(4)
                .build());
        when(careContextService.isValid(anyString())).thenReturn(false);
        when(careContextService.careContextForPatient(anyInt()))
                .thenReturn(patientCareContextList);


        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientId", "72aa")
                .header("authorization", "Basic c3VwZXJtYW46QWRtaW4xMjM=")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        String responseBody = new ObjectMapper().writeValueAsString(ClientError.invalidPatientId());
        assertEquals(responseBody, content);
    }

    @Test
    public void shouldReturn400BadRequestWhenNoPatientIdProvided() throws Exception {
        List<PatientCareContext> patientCareContextList = new ArrayList<>();
        patientCareContextList.add(PatientCareContext.builder()
                .careContextName("TB Program")
                .careContextType("PROGRAM")
                .careContextReference(4)
                .build());
        when(careContextService.isValid(anyString())).thenReturn(true);
        when(careContextService.careContextForPatient(anyInt()))
                .thenReturn(patientCareContextList);


        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientId", "")
                .header("authorization", "Basic c3VwZXJtYW46QWRtaW4xMjM=")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        String responseBody = new ObjectMapper().writeValueAsString(ClientError.noPatientIdProvided());
        assertEquals(responseBody, content);
    }

    @Test
    public void shouldReturnUnauthorizedErrorMessageWhenNoAuth() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        assertEquals("{\"code\":1504,\"message\":\"User is not authorized\"}", content);
    }
}
