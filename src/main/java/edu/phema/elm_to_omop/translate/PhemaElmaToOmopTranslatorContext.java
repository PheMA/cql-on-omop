package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.hl7.elm.r1.Library;
import org.hl7.elm.r1.Retrieve;
import org.hl7.elm.r1.ValueSetDef;
import org.hl7.elm.r1.ValueSetRef;

import java.util.List;
import java.util.Optional;

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

    public ValueSetDef getValueset(String valuesetReference) throws PhemaTranslationException {
        Optional<ValueSetDef> valueset = library
            .getValueSets()
            .getDef()
            .stream()
            .filter(vd -> vd.getName().equals(valuesetReference))
            .findFirst();

        if (valueset.isPresent()) {
            return valueset.get();
        } else {
            throw new PhemaTranslationException(String.format("Value set %s not found", valuesetReference));
        }
    }

    public int getCodesetId(String valuesetReference) throws PhemaTranslationException {
        ValueSetDef valueset = getValueset(valuesetReference);

        Optional<PhemaConceptSet> result = conceptSets
            .stream()
            .filter(c -> c.getOid().equals(valueset.getId()))
            .findFirst();

        if (result.isPresent()) {
            return result.get().id;
        } else {
            throw new PhemaTranslationException(String.format("Value set %s not found", valuesetReference));
        }
    }

    public int getCodesetIdForRetrieve(Retrieve retrieve) throws PhemaTranslationException {
        String valuesetReference = ((ValueSetRef) retrieve.getCodes()).getName();

        return getCodesetId(valuesetReference);
    }
}
