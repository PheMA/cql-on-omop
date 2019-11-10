package edu.phema.elm_to_omop.api.exception;

public class CqlStatementNotFoundException extends Exception {
    public CqlStatementNotFoundException(String message) {
        super(message);
    }

    public CqlStatementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}