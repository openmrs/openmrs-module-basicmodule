package org.bahmni.module.hip.web.service;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicommons.api.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicommons.api.dao.PatientDao;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationServiceImpl;
import org.bahmni.module.hip.Config;
import org.bahmni.module.hip.api.dao.ExistingPatientDao;
import org.bahmni.module.hip.web.client.model.Status;
import org.bahmni.module.hip.web.model.ExistingPatient;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;


@Service
public class ExistingPatientService {
    private final ExistingPatientDao existingPatientDao;
    static final int MATCHING_CRITERIA_CONSTANT = 2;
    private final PatientDao patientDao;
    private final PatientService patientService;
    private final LocationService locationService;
    static final int PHONE_NUMBER_LENGTH = 10;

    @Autowired
    public ExistingPatientService(PatientDao patientDao, PatientService patientService, ExistingPatientDao existingPatientDao, LocationService locationService) {
        this.patientDao = patientDao;
        this.patientService = patientService;
        this.existingPatientDao = existingPatientDao;
        this.locationService = locationService;
    }

    public Set<Patient> getMatchingPatients(String locationUuid,String phoneNumber, String patientName, int patientYearOfBirth, String patientGender) {
        Set<Patient> matchingPatients = new HashSet<>();
        matchingPatients.addAll(getMatchingPatients(phoneNumber));
        matchingPatients.addAll(getMatchingPatients(locationUuid,patientName, patientYearOfBirth, patientGender));
        matchingPatients.removeIf(patient -> !getHealthId(patient).equals(""));
        return matchingPatients;
    }

    public String getHealthId(Patient patient) {
        String healthId = "";
        try {
            healthId = patient.getPatientIdentifier(Config.ABHA_ADDRESS.getValue()).getIdentifier();
        } catch (NullPointerException ignored) {

        }
        return healthId;
    }

    public void perform(String healthId, String action) {
        Patient patient = patientService.getPatientByUuid(getPatientWithHealthId(healthId));
        PatientIdentifier patientIdentifierPhr = patient.getPatientIdentifier(Config.ABHA_ADDRESS.getValue());
        if (action.equals(Status.DELETED.toString())) {
            removeHealthId(patient,patientIdentifierPhr);
        }
        if (action.equals(Status.DEACTIVATED.toString())) {
            voidHealthId(patientIdentifierPhr);
        }
        if (action.equals(Status.REACTIVATED.toString())) {
            unVoidHealthId(patient,healthId);
        }
    }

    private void voidHealthId(PatientIdentifier patientIdentifierPHR) {
        try {
            if (!patientIdentifierPHR.getVoided()) {
                patientService.voidPatientIdentifier(patientIdentifierPHR, Status.DEACTIVATED.toString());
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void unVoidHealthId(Patient patient, String phrAddress) {
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        try {
            for (PatientIdentifier patientIdentifier : patientIdentifiers) {
                if (patientIdentifier.getIdentifierType().getName().equals(Config.ABHA_ADDRESS.getValue())) {
                    if(patientIdentifier.getVoided()){
                        patientIdentifier.setVoided(false);
                        patientService.savePatientIdentifier(patientIdentifier);
                    }
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    private void removeHealthId(Patient patient,PatientIdentifier patientIdentifierPHR) {
        try {
            if(patientIdentifierPHR != null)
                patient.removeIdentifier(patientIdentifierPHR);
            patientService.savePatient(patient);
        } catch (NullPointerException ignored) {
        }
    }

    public List<Patient> getMatchingPatients(String phoneNumber) {
        if(!phoneNumber.equals("undefined"))
            return existingPatientDao.getPatientsWithPhoneNumber(phoneNumber.substring(phoneNumber.length() - PHONE_NUMBER_LENGTH));
        return new ArrayList<>();
    }

    public List<Patient> getMatchingPatients(String locationUuid, String patientName, int patientYearOfBirth, String patientGender) {
        List<PatientResponse> patients = getPatients(locationUuid,patientName, patientYearOfBirth, patientGender);
        List<Patient> existingPatients = new ArrayList<>();
        for (PatientResponse patient : patients) {
            existingPatients.add(patientService.getPatientByUuid(patient.getUuid()));
        }
        return existingPatients;
    }

    private List<PatientResponse> getPatients(String locationUuid, String patientName, int patientYearOfBirth, String patientGender) {
        List<PatientResponse> patientsMatchedWithName = filterPatientsByName(locationUuid,patientName);
        if (patientsMatchedWithName.size() != 1) {
            List<PatientResponse> patientsMatchedWithNameAndAge = filterPatientsByAge(patientYearOfBirth, patientsMatchedWithName);
            if (patientsMatchedWithNameAndAge.size() != 1)
                return filterPatientsByGender(patientGender, patientsMatchedWithNameAndAge);
            return patientsMatchedWithNameAndAge;
        }
        return patientsMatchedWithName;
    }

    private List<PatientResponse> filterPatientsByName(String locationUuid,String patientName) {
        PatientSearchParameters searchParameters = getPatientSearchParameters(locationUuid,patientName);
        Supplier<Location> visitLocation = () -> getVisitLocation(locationUuid);
        Supplier<List<String>> configuredAddressFields = () -> patientDao.getConfiguredPatientAddressFields();

        return patientDao.getPatients(searchParameters, visitLocation, configuredAddressFields);
    }


    private List<PatientResponse> filterPatientsByAge(int patientYearOfBirth, List<PatientResponse> patientsMatchedWithNameAndGender) {
        List<PatientResponse> patients = new ArrayList<>();
        for (PatientResponse patient : patientsMatchedWithNameAndGender) {
            if (verifyYearOfBirth(getYearOfBirth(patient.getBirthDate()), patientYearOfBirth)) {
                patients.add(patient);
            }
        }
        return patients;
    }

    private Integer getYearOfBirth(Date birthDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthDate);
        return calendar.get(Calendar.YEAR);
    }

    private boolean verifyYearOfBirth(int yearOfBirth, int patientYearOfBirth) {
        return yearOfBirth == patientYearOfBirth || Math.abs(yearOfBirth - patientYearOfBirth) <= MATCHING_CRITERIA_CONSTANT;
    }

    private List<PatientResponse> filterPatientsByGender(String patientGender, List<PatientResponse> patientMatchedWithName) {
        List<PatientResponse> patients = new ArrayList<>();
        for (PatientResponse patient : patientMatchedWithName) {
            if (patient.getGender().equals(patientGender))
                patients.add(patient);
        }
        return patients;
    }

    public List<ExistingPatient> getMatchingPatientDetails(Set<Patient> matchingPatients) {
        List<ExistingPatient> existingPatients = new ArrayList<>();
        for (Patient patient : matchingPatients) {
            if (!isHealthIdVoided(patient.getUuid())) {
                existingPatients.add(
                        new ExistingPatient(
                                patient.getGivenName() + " " + patient.getMiddleName() + " " + patient.getFamilyName(),
                                patient.getBirthdate().toString(),
                                getAddress(patient),
                                patient.getGender(),
                                patient.getUuid(),
                                getPhoneNumber(patient))
                );
            }
        }
        return existingPatients;
    }

    private String getPhoneNumber(Patient patient) {
        String phoneNumber = " ";
        try {
            phoneNumber = patient.getAttribute(Config.PHONE_NUMBER.getValue()).getValue();
        } catch (NullPointerException ignored) {

        }
        return phoneNumber;
    }

    private String getAddress(Patient patient) {
        if (patient.getPersonAddress() != null) {
            return patient.getPersonAddress().getAddress1() +
                    "," + patient.getPersonAddress().getCountyDistrict() +
                    "," + patient.getPersonAddress().getStateProvince();
        }
        return "";
    }

    public String getPatientWithHealthId(String healthId) {
        return existingPatientDao.getPatientUuidWithHealthId(healthId);
    }

    public boolean isHealthIdVoided(String uuid){
        Patient patient = patientService.getPatientByUuid(uuid);
        Set<PatientIdentifier> patientIdentifiers = patient.getIdentifiers();
        try {
            for (PatientIdentifier patientIdentifier:patientIdentifiers) {
                if(patientIdentifier.getIdentifierType().getName().equals(Config.ABHA_ADDRESS.getValue())){
                   return patientIdentifier.getVoided();
                }
            }
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    private PatientSearchParameters getPatientSearchParameters(String locationUuid,String patientName) {
        PatientSearchParameters searchParameters = new PatientSearchParameters();
        searchParameters.setIdentifier("");
        searchParameters.setName(patientName);
        searchParameters.setCustomAttribute(null);

        searchParameters.setAddressFieldName(null);
        searchParameters.setAddressFieldValue("");
        searchParameters.setLength(100);
        searchParameters.setStart(0);

        searchParameters.setPatientAttributes(null);
        searchParameters.setProgramAttributeFieldName("");
        searchParameters.setProgramAttributeFieldValue(null);
        searchParameters.setAddressSearchResultFields(null);
        searchParameters.setPatientSearchResultFields(null);

        searchParameters.setLoginLocationUuid(locationUuid);
        searchParameters.setFilterPatientsByLocation(false);
        searchParameters.setFilterOnAllIdentifiers(false);
        return searchParameters;
    }
    private Location getVisitLocation(String loginLocationUuid) {
        if (StringUtils.isBlank(loginLocationUuid)) {
            return null;
        }
        BahmniVisitLocationServiceImpl bahmniVisitLocationService = new BahmniVisitLocationServiceImpl(Context.getLocationService());
        return bahmniVisitLocationService.getVisitLocation(loginLocationUuid);
    }
}
