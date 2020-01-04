package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import org.hl7.elm.r1.Code;

import java.util.List;

/**
 * Valueset service that resolves a list of ELM codes and creates an
 * OMOP concept set for each one
 */
public class ElmCodeResolvingValuesetService implements IValuesetService {
    private List<Code> codes;

    public ElmCodeResolvingValuesetService(List<Code> codes) {
        this.codes = codes;
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        PhemaConceptSetList phemaConceptSetList = new PhemaConceptSetList();


        return phemaConceptSetList;
    }

    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        return getConceptSetList().getConceptSets();
    }
}
