package edu.phema.elm_to_omop.vocabulary;

public class ValuesetServiceException extends Exception {
    public ValuesetServiceException(String message) {
        super(message);
    }

    public ValuesetServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}