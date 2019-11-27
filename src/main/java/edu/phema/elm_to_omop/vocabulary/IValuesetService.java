package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;

import java.util.List;

public interface IValuesetService {
    List<PhemaConceptSet> getConceptSets() throws Exception;
}
