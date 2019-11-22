package edu.phema.elm_to_omop.valueset;

import edu.phema.elm_to_omop.model.omop.ConceptSet;
import org.ohdsi.circe.vocabulary.Concept;

import java.util.List;

public interface IValuesetService {
    List<ConceptSet> getConceptSets() throws Exception;
}
