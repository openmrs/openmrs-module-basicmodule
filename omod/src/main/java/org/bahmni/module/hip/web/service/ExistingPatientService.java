package org.bahmni.module.hip.web.service;

import org.apache.commons.lang.StringUtils;
import org.bahmni.module.hip.web.model.ExistingPatient;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class ExistingPatientService {
    private final PatientService patientService;
    static final int MATCHING_CRITERIA_CONSTANT = 2;

    @Autowired
    public ExistingPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public List<Patient> getMatchingPatients(String patientName, int patientYearOfBirth,
                                             String patientGender) {
        return getPatients(patientName, patientYearOfBirth, patientGender);
    }

    private List<Patient> getPatients(String patientName, int patientYearOfBirth, String patientGender) {
        List<Patient> patientsMatchedWithName = filterPatientsByName(patientName);
        if (patientsMatchedWithName.size() != 1) {
            List<Patient> patientsMatchedWithNameAndAge = filterPatientsByAge(patientYearOfBirth, patientsMatchedWithName);
            if (patientsMatchedWithNameAndAge.size() != 1)
                return filterPatientsByGender(patientGender, patientsMatchedWithNameAndAge);
            return patientsMatchedWithNameAndAge;
        }
        return patientsMatchedWithName;
    }

    private List<Patient> filterPatientsByName(String patientName) {
        List<Patient> existingPatients = patientService.getAllPatients();
        List<Patient> patients = new ArrayList<>();
        for (Patient patient : existingPatients) {
            String givenName = patientName.split(" ")[0].toLowerCase();
            String familyName = patientName.split(" ")[1].toLowerCase();
            int distanceOfGivenName = StringUtils.getLevenshteinDistance(givenName, patient.getGivenName().toLowerCase());
            int distanceOfFamilyName = StringUtils.getLevenshteinDistance(familyName, patient.getFamilyName().toLowerCase());
            if (distanceOfGivenName <= MATCHING_CRITERIA_CONSTANT
                    && (distanceOfFamilyName <= MATCHING_CRITERIA_CONSTANT
                    || patient.getFamilyName().charAt(0) == patientName.split(" ")[1].charAt(0)))
                patients.add(patient);
        }
        return patients;
    }

    private List<Patient> filterPatientsByAge(int patientYearOfBirth, List<Patient> patientsMatchedWithNameAndGender) {
        List<Patient> patients = new ArrayList<>();
        for (Patient patient : patientsMatchedWithNameAndGender) {
            if (verifyYearOfBirth(getYearOfBirth(patient.getAge()), patientYearOfBirth)) {
                patients.add(patient);
            }
        }
        return patients;
    }

    private Integer getYearOfBirth(int age) {
        return Calendar.getInstance().get(Calendar.YEAR) - age;
    }

    private boolean verifyYearOfBirth(int yearOfBirth, int patientYearOfBirth) {
        return yearOfBirth == patientYearOfBirth || Math.abs(yearOfBirth - patientYearOfBirth) <= MATCHING_CRITERIA_CONSTANT;
    }

    private List<Patient> filterPatientsByGender(String patientGender, List<Patient> patientMatchedWithName) {
        List<Patient> patients = new ArrayList<>();
        for (Patient patient : patientMatchedWithName) {
            if (patient.getGender().equals(patientGender))
                patients.add(patient);
        }
        return patients;
    }

    public ExistingPatient getMatchingPatientDetails(List<Patient> matchingPatients) {
        Patient patient = matchingPatients.get(0);
        ExistingPatient existingPatient = new ExistingPatient(patient.getGivenName() + " " + patient.getFamilyName(),
                getYearOfBirth(patient.getAge()).toString(),
                patient.getPersonAddress().getAddress1() +
                        "," + patient.getPersonAddress().getCountyDistrict() +
                        "," + patient.getPersonAddress().getStateProvince(),
                patient.getGender());

        return existingPatient;
    }
}
