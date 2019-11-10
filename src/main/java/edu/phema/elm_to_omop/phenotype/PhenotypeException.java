package edu.phema.elm_to_omop.phenotype;

public class PhenotypeException extends Exception {
    public PhenotypeException(String message) {
        super(message);
    }

    public PhenotypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
