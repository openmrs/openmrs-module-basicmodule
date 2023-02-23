package org.bahmni.module.hip.web.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ErrorCode {
    UNKNOWN_ERROR_OCCURRED(1500),
    BAD_REQUEST(1501),
    PATIENT_ID_NOT_FOUND(1502),
    NO_PATIENT_ID_SUPPLIED (1503),
    NO_VISIT_TYPE_SUPPLIED(1504),
    INVALID_VISIT_TYPE(1505),
    NO_PROGRAM_NAME_SUPPLIED (1506),
    NO_PROGRAM_ID_SUPPLIED(1507),
    INVALID_PROGRAM_NAME(1508),
    PATIENT_IDENTIFIER_NOT_FOUND(1509),
    NO_VISIT_START_DATE_SUPPLIED(1510);
    private final int value;

    ErrorCode(int val) {
        value = val;
    }

    // Adding @JsonValue annotation that tells the 'value' to be of integer type while de-serializing.
    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static ErrorCode getNameByValue(int value) {
        return Arrays.stream(ErrorCode.values())
                .filter(errorCode -> errorCode.value == value)
                .findAny()
                .orElse(ErrorCode.UNKNOWN_ERROR_OCCURRED);
    }
}
