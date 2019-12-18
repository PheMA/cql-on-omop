package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.hl7.elm.r1.Library;

import java.util.List;

public class PhemaElmaToOmopTranslatorContext {
    private Library library;
    private List<PhemaConceptSet> conceptSets;

    public PhemaElmaToOmopTranslatorContext(Library library, List<PhemaConceptSet> conceptSets) {
        this.library = library;
        this.conceptSets = conceptSets;
    }

    public Library getLibrary() {
        return library;
    }

    public List<PhemaConceptSet> getConceptSets() {
        return conceptSets;
    }
}
