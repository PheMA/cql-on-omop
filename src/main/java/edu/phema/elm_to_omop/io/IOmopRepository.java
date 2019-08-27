package edu.phema.elm_to_omop.io;

import edu.phema.elm_to_omop.model.omop.Concept;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public interface IOmopRepository {
    Concept getConceptMetadata(String domain, String source, String id) throws IOException, ParseException;
    String postCohortDefinition(String domain, String json) throws IOException, ParseException;
    String generateCohort(String domain, String id, String source) throws IOException, ParseException;
    String getExecutionStatus(String domain, String id) throws IOException, ParseException;
    String getCohortCount(String domain, String id, String source) throws IOException, ParseException;
}
