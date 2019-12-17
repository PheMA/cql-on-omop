package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;

import java.util.ArrayList;
import java.util.List;

/**
 * Empty value set service. Useful in cases
 * where you are using criteria that rely on
 * terminologies (e.g. Demographic criteria)
 */
public class EmptyValuesetService implements IValuesetService {
    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        return new ArrayList<>();
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        return new PhemaConceptSetList();
    }
}
