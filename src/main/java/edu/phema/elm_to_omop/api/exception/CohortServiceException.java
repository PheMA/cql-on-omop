package edu.phema.elm_to_omop.api.exception;

public class CohortServiceException extends Exception {
    public CohortServiceException(String message) {
        super(message);
    }

    public CohortServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}


