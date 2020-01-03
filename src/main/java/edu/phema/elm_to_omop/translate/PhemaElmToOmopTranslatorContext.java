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

    public int getCodeSetIdForCode(String code) throws PhemaTranslationException {
        Optional<PhemaConceptSet> result = conceptSets
            .stream()
            .filter(c -> c.getOid().equals(code))
            .findFirst();

        if (result.isPresent()) {
            return result.get().id;
        } else {
            throw new PhemaTranslationException(String.format("Codeset not found for code %d", code));
        }
    }

    /**
     * This occurs when we are filtering a Retrieve using a single code instead of a value set. The ELM generated
     * is of the form ToList(ToConcept(Code)), so we need to extract the code, then type to find a valueset containing
     * just the given code.
     *
     * @param toList The ELM ToList expression wrapping the code
     * @return The ID of the valueset provided by the ValuesetService implementation
     * @throws PhemaTranslationException
     */
    public int getCodesetIdForToList(ToList toList) throws PhemaTranslationException {
        ToConcept toConcept = (ToConcept) toList.getOperand();

        CodeRef codeRef = (CodeRef) toConcept.getOperand();

        CodeDef code = getCode(codeRef.getName());

        return getCodeSetIdForCode(code.getName());
    }

    public int getCodesetIdForRetrieve(Retrieve retrieve) throws PhemaTranslationException {
        Expression codeExpression = retrieve.getCodes();

        if (codeExpression instanceof ValueSetRef) {
            // The retrieve references a value set
            String valuesetReference = ((ValueSetRef) retrieve.getCodes()).getName();
            return getCodesetId(valuesetReference);
        } else if (codeExpression instanceof ToList) {
            return getCodesetIdForToList((ToList) codeExpression);
        } else {
            throw new PhemaTranslationException(String.format("Unable to retrieve codesetId for code expression of type %s", codeExpression.getClass().getSimpleName()));
        }
    }
}
