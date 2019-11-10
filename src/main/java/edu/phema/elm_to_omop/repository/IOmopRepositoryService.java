package edu.phema.elm_to_omop.repository;

import edu.phema.elm_to_omop.model.omop.Concept;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Represents and instance of an OMOP server
 */
public interface IOmopRepositoryService {
    Concept getConceptMetadata(String id) throws IOException, ParseException;

    String postCohortDefinition(String cohortDefinitionJson) throws IOException, ParseException;

    String generateCohort(String id) throws IOException, ParseException;

    String getExecutionStatus(String id) throws IOException, ParseException;

    String getCohortCount(String id) throws IOException, ParseException;
}
