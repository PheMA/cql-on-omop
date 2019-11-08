package edu.phema.elm_to_omop.api.exception;

public class CqlStatementNotFoundException extends Exception {
  public CqlStatementNotFoundException(String errorMessage) {
    super(errorMessage);
  }
}