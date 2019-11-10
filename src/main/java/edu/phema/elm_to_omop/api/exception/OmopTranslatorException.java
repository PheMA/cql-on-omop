package edu.phema.elm_to_omop.api.exception;

public class OmopTranslatorException extends Exception {
    public OmopTranslatorException(String message) {
        super(message);
    }

    public OmopTranslatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
