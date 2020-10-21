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
        return new ClientError(ErrorCode.NO_PATIENT_ID_SUPPLIED.getValue(), "No patient id supplied");
    }
}
