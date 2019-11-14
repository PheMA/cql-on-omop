package edu.phema.elm_to_omop.repository;

public class OmopRepositoryException extends Exception {
    public OmopRepositoryException(String message) {
        super(message);
    }

    public OmopRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
