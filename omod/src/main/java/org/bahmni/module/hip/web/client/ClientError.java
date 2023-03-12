package org.bahmni.module.hip.web.client;

import org.bahmni.module.hip.web.client.model.ErrorCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;

@Getter
@ToString
public class ClientError {
    private static HashMap<String, String> errorMap = new HashMap<>();
    private Integer code;
    private String message;

    public ClientError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ClientError invalidPatientId() {
        return new ClientError(ErrorCode.BAD_REQUEST.getValue(), "Invalid patient id");
    }

    public static ClientError noPatientFound() {
        return new ClientError(ErrorCode.PATIENT_ID_NOT_FOUND.getValue(), "No patient found");
    }

    public static ClientError noPatientIdProvided() {
        return new ClientError(ErrorCode.NO_PATIENT_ID_SUPPLIED.getValue(), "No Patient ID supplied");
    }
    public static ClientError noVisitTypeProvided(){
        return new ClientError(ErrorCode.NO_VISIT_TYPE_SUPPLIED.getValue(),"No Visit type is supplied");
    }
    public static ClientError noVisitStartDateProvided(){
        return new ClientError(ErrorCode.NO_VISIT_START_DATE_SUPPLIED.getValue(),"No Visit start date is supplied");
    }
    public static ClientError invalidVisitType(){
        return new ClientError(ErrorCode.INVALID_VISIT_TYPE.getValue(),"Visit Type is invalid");
    }
    public static ClientError noProgramNameProvided(){
        return new ClientError(ErrorCode.NO_PROGRAM_NAME_SUPPLIED.getValue(),"No program name supplied");
    }
    public static ClientError noProgramIDProvided(){
        return new ClientError(ErrorCode.NO_PROGRAM_ID_SUPPLIED.getValue(),"No patient's program enrollment id supplied");
    }
    public static ClientError invalidProgramName(){
        return new ClientError(ErrorCode.INVALID_PROGRAM_NAME.getValue(),"Program specified does not exist");
    }
    public static ClientError patientIdentifierNotFound(){
        return new ClientError(ErrorCode.PATIENT_IDENTIFIER_NOT_FOUND.getValue(),"Patient identifier not found");
    }

    public static ClientError invalidStartDate() {
        return new ClientError(ErrorCode.INVALID_START_DATE_SUPPLIED.getValue(),"Invalid start date specified");
    }
    public static ClientError invalidEndDate() {
        return new ClientError(ErrorCode.INVALID_END_DATE_SUPPLIED.getValue(),"Invalid end date specified");
    }

}
