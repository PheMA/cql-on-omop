package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;

import java.util.List;

public interface IValuesetService {
    List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException;

    PhemaConceptSetList getConceptSetList() throws ValuesetServiceException;
}
