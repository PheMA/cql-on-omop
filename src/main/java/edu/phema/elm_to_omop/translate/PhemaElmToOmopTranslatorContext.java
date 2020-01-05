package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import org.hl7.elm.r1.*;

import java.util.List;
import java.util.Optional;

public class PhemaElmToOmopTranslatorContext {
    private Library library;
    private List<PhemaConceptSet> conceptSets;

    public PhemaElmToOmopTranslatorContext(Library library, List<PhemaConceptSet> conceptSets) {
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

    public CodeDef getCode(String codeReference) throws PhemaTranslationException {
        Optional<CodeDef> codeDef = library
            .getCodes()
            .getDef()
            .stream()
            .filter(cd -> cd.getName().equals(codeReference))
            .findFirst();

        if (codeDef.isPresent()) {
            return codeDef.get();
        } else {
            throw new PhemaTranslationException(String.format("Code %s not found", codeReference));
        }
    }

    /**
     * Return the appropriate vocabulary reference depending on whether we are dealing with a value set or a code
     *
     * @param vocabularyReference The reference used in CQL
     * @return The OID we use internally to identify the vocabulary reference
     * @throws PhemaTranslationException
     */
    private String getInternalVocabularyReference(String vocabularyReference) throws PhemaTranslationException {
        try {
            ValueSetDef valueSetDef = getValueset(vocabularyReference);

            return valueSetDef.getId();
        } catch (PhemaTranslationException e) { /* Noop */ }

        try {
            CodeDef codeDef = getCode(vocabularyReference);

            return codeDef.getName();
        } catch (PhemaTranslationException e) {
            throw new PhemaTranslationException(String.format("No valueset or code found for vocabulary reference %s", vocabularyReference));
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

    public int getCodeSetIdForVocabularyReference(String vocabularyReference) throws PhemaTranslationException {
        String internalVocabularyReference = getInternalVocabularyReference(vocabularyReference);

        Optional<PhemaConceptSet> result = conceptSets
            .stream()
            .filter(c -> c.getOid().equals(internalVocabularyReference))
            .findFirst();

        if (result.isPresent()) {
            return result.get().id;
        } else {
            throw new PhemaTranslationException(String.format("Codeset not found for code %s", vocabularyReference));
        }
    }

    public String getVocabularyReferenceForRetrieve(Retrieve retrieve) throws PhemaTranslationException {
        Expression codeExpression = retrieve.getCodes();

        if (codeExpression instanceof ValueSetRef) {
            // The retrieve references a value set
            return ((ValueSetRef) retrieve.getCodes()).getName();
        } else if (codeExpression instanceof ToList) {
            // The retrieve references a code
            ToList toList = (ToList) codeExpression;
            ToConcept toConcept = (ToConcept) toList.getOperand();
            CodeRef codeRef = (CodeRef) toConcept.getOperand();

            return codeRef.getName();
        } else {
            throw new PhemaTranslationException(String.format("Unable to retrieve codesetId for code expression of type %s", codeExpression.getClass().getSimpleName()));
        }
    }

    public int getCodesetIdForRetrieve(Retrieve retrieve) throws PhemaTranslationException {
        String vocabularyReference = getVocabularyReferenceForRetrieve(retrieve);

        return getCodeSetIdForVocabularyReference(vocabularyReference);
    }
}
