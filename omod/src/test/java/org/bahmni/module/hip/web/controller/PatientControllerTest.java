package org.bahmni.module.hip.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.bahmni.module.hip.web.TestConfiguration;
import org.bahmni.module.hip.web.client.ClientError;
import org.bahmni.module.hip.web.client.model.Error;
import org.bahmni.module.hip.web.client.model.ErrorCode;
import org.bahmni.module.hip.web.client.model.ErrorRepresentation;
import org.bahmni.module.hip.web.model.ExistingPatient;
import org.bahmni.module.hip.web.service.ExistingPatientService;
import org.bahmni.module.hip.web.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Patient;
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

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PatientController.class, TestConfiguration.class})
@WebAppConfiguration
public class PatientControllerTest extends TestCase {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ExistingPatientService existingPatientService;

    @Autowired
    private ValidationService validationService;

    @Before
    public void setup() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(this.wac);
        this.mockMvc = builder.build();
    }

    @Test
    public void shouldReturn200OKWhenMatchingPatientFound() throws Exception {
        Patient patient = mock(Patient.class);
        List<Patient> patients = new ArrayList<>();
        patients.add(patient);
        ExistingPatient existingPatient = new ExistingPatient("sam tom", "35", "null, null", "M", "3f81c3b4-04fc-4311-9b50-b863fbe023dc", "9123456780");
        when(existingPatientService.getMatchingPatients("+91-9876543210"))
                .thenReturn(new ArrayList<>());
        when(existingPatientService.getMatchingPatients(anyString(),anyString(), anyInt(), anyString()))
                .thenReturn(patients);
        when(existingPatientService.getMatchingPatientDetails(new HashSet<>(patients)))
                .thenReturn(Collections.singletonList(existingPatient));

        mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients", RestConstants.VERSION_1))
                .param("patientName", "sam tom")
                .param("patientYearOfBirth", "1985")
                .param("patientGender", "M")
                .param("phoneNumber", "+91-9876543210")
                .cookie(new Cookie("bahmni.user.location", "{\"name\":\"General Ward\",\"uuid\":\"baf7bd38-d225-11e4-9c67-080027b662ec\"}"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnNoRecordsWhenNoMatchingPatientFound() throws Exception {
        List<Patient> patients = new ArrayList<>();
        when(existingPatientService.getMatchingPatients("+91-9876543210"))
                .thenReturn(new ArrayList<>());
        when(existingPatientService.getMatchingPatients(anyString(),anyString(), anyInt(), anyString()))
                .thenReturn(patients);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients", RestConstants.VERSION_1))
                .param("patientName", "sam tom")
                .param("patientYearOfBirth", "1985")
                .param("patientGender", "M")
                .param("phoneNumber", "+91-9876543210")
                .cookie(new Cookie("bahmni.user.location", "{\"name\":\"General Ward\",\"uuid\":\"baf7bd38-d225-11e4-9c67-080027b662ec\"}"))
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(new ErrorRepresentation
                (new Error(ErrorCode.PATIENT_ID_NOT_FOUND, "No patient found")));
        assertEquals(value,
                mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnPatientUuidWhenTheHealthIdIsLinkedToAPatient() throws Exception {
        when(existingPatientService.getPatientWithHealthId("abc.xyz@sbx")).thenReturn("bd27cbfd-b395-4a8a-af71-b27535b85e31");
        mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients/" + "abc.xyz@sbx", RestConstants.VERSION_1))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnNoPatientFoundAsResponseWhenTheHealthIdIsNotLinkedToAnyPatient() throws Exception {
        when(existingPatientService.getPatientWithHealthId("def.xyz@sbx")).thenReturn(null);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients/" + "def.xyz@sbx", RestConstants.VERSION_1))
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(new ErrorRepresentation
                (new Error(ErrorCode.PATIENT_ID_NOT_FOUND, "No patient found")));
        assertEquals(value,
                mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnNoPatientIdentifierFoundIfHealthIsNotPresent() throws Exception {
        when(validationService.isValidHealthId("test@sbx")).thenReturn(false);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients/status", RestConstants.VERSION_1))
                .param("healthId", "test@sbx")
                .param("action", "DELETED")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(ClientError.patientIdentifierNotFound());
        assertEquals(value,
                mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void shouldReturnNoPatientIdentifierFoundIfHealthIsNotPresentInDeactivation() throws Exception {
        when(validationService.isValidHealthId("test@sbx")).thenReturn(false);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients/status", RestConstants.VERSION_1))
                .param("healthId", "test@sbx")
                .param("action", "DEACTIVATED")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        String value = objectMapper.writeValueAsString(ClientError.patientIdentifierNotFound());
        assertEquals(value,
                mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void shouldNotReturnErrorWhenHealthIdIsRemoved() throws Exception {
        when(validationService.isValidHealthId("test@sbx")).thenReturn(true);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients/status", RestConstants.VERSION_1))
                .param("healthId", "test@sbx")
                .param("action", "DELETED")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals("",
                mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void shouldNotReturnErrorWhenHealthIdIsDeactivated() throws Exception {
        when(validationService.isValidHealthId("test@sbx")).thenReturn(true);

        MvcResult mvcResult = mockMvc.perform(get(String.format("/rest/%s/hip/existingPatients/status", RestConstants.VERSION_1))
                .param("healthId", "test@sbx")
                .param("action", "DEACTIVATED")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        assertEquals("",
                mvcResult.getResponse().getContentAsString());
    }
}