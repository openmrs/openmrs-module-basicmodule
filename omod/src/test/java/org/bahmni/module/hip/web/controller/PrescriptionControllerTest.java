package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.service.PrescriptionService;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
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

import static java.util.Collections.EMPTY_LIST;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PrescriptionController.class, TestConfiguration.class})
@WebAppConfiguration
public class PrescriptionControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PrescriptionService prescriptionService;

    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    public void shouldReturn200OkWhenfromDateToDateAndPatientIdAreGiven() throws Exception {
        when(prescriptionService.getPrescriptions(anyString(), any(), anyString()))
                .thenReturn(EMPTY_LIST);
        mockMvc.perform(get(String.format("/rest/%s/hip/prescriptions", RestConstants.VERSION_1))
                .param("visitType", "IPD")
                .param("patientId", "'0f90531a-285c-438b-b265-bb3abb4745bd'")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnPatientIdRequestParameterIsMandatoryErrorMessage() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/prescriptions", RestConstants.VERSION_1))
                .param("visitType", "IPD")
                .param("fromDate", "2020-01-01")
                .param("toDate", "2020-01-31")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(500, mvcResult.getResponse().getStatus());
    }
}
