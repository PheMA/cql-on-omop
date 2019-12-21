package edu.phema.elm_to_omop.translate.criteria.correlation;

public class CorrelationException extends Exception {
    public CorrelationException(String message) {
        super(message);
    }

    public CorrelationException(String message, Throwable cause) {
        super(message, cause);
    }
}