package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.model.PatientCareContext;
import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.model.serializers.NewCareContext;
import org.bahmni.module.hip.web.service.CareContextService;
import org.bahmni.module.hip.web.service.ValidationService;
import org.codehaus.jackson.map.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
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

    @Autowired
    private ValidationService validationService;

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
                .careContextReference("SUPER MAN")
                .build());
        when(careContextService.careContextForPatient(anyString()))
                .thenReturn(patientCareContextList);
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);

        mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientUuid", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400BadRequestWhenPatientIdContainsCharacters() throws Exception {
        List<PatientCareContext> patientCareContextList = new ArrayList<>();
        patientCareContextList.add(PatientCareContext.builder()
                .careContextName("TB Program")
                .careContextType("PROGRAM")
                .careContextReference("SUPER MAN")
                .build());
        when(careContextService.careContextForPatient(anyString()))
                .thenReturn(patientCareContextList);


        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientUuid", "72aa")
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
                .careContextReference("SUPER MAN")
                .build());
        when(careContextService.careContextForPatient(anyString()))
                .thenReturn(patientCareContextList);


        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientUuid", " ")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        String responseBody = new ObjectMapper().writeValueAsString(ClientError.noPatientIdProvided());
        assertEquals(responseBody, content);
    }

    @Test
    public void shouldReturn400BadRequestWhenNoPatientIdProvidedForNewContext() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext/new", RestConstants.VERSION_1))
                .param("patientUuid", " ")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        String responseBody = new ObjectMapper().writeValueAsString(ClientError.noPatientIdProvided());
        assertEquals(responseBody, content);
    }

    @Test
    public void shouldReturn400BadRequestForInvalidPatientUUID() throws Exception {
        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(false);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/careContext/new", RestConstants.VERSION_1))
                .param("patientUuid", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        String responseBody = new ObjectMapper().writeValueAsString(ClientError.invalidPatientId());
        assertEquals(responseBody, content);
    }

    @Test
    public void shouldReturn200OKForValidPatientUUID() throws Exception {
        NewCareContext newCareContext = new NewCareContext("abc", "abc@sbc", "12gvx", new ArrayList<>());

        when(validationService.isValidPatient("0f90531a-285c-438b-b265-bb3abb4745bd")).thenReturn(true);
        when(careContextService.newCareContextsForPatient(anyString()))
                .thenReturn(newCareContext);

        mockMvc.perform(get(String.format("/rest/%s/hip/careContext/new", RestConstants.VERSION_1))
                .param("patientUuid", "0f90531a-285c-438b-b265-bb3abb4745bd")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

    }
}
