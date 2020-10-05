package org.bahmni.module.hip.web.exception;

public class RequestParameterMissingException extends RuntimeException {

    private String parameterName;

    public RequestParameterMissingException(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public String getMessage() {
        return parameterName + " is mandatory request parameter";
    }
}
