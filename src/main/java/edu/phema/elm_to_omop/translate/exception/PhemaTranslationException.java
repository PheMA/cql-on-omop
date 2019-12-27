package edu.phema.elm_to_omop.translate.exception;

public class PhemaTranslationException extends Exception {
    public PhemaTranslationException(String message) {
        super(message);
    }

    public PhemaTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
