package org.bahmni.module.hip.web.controller;

import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.service.CareContextService;
import org.bahmni.module.hip.web.service.CareContextServiceTest;
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
        when(careContextService.careContextForPatient(anyString()))
                .thenReturn(EMPTY_LIST);

        mockMvc.perform(get(String.format("/rest/%s/hip/careContext", RestConstants.VERSION_1))
                .param("patientId", "72")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
