package edu.phema.elm_to_omop.vocabulary;

import edu.phema.elm_to_omop.repository.IOmopRepositoryService;
import edu.phema.elm_to_omop.repository.OmopRepositoryException;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSet;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaConceptSetList;
import edu.phema.elm_to_omop.vocabulary.translate.PhemaVocabularyTranslator;
import org.hl7.elm.r1.CodeDef;
import org.hl7.elm.r1.CodeSystemDef;
import org.hl7.elm.r1.CodeSystemRef;
import org.hl7.elm.r1.Library;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Valueset service that resolves a list of codes individually specified in an ELM library and creates an OMOP concept
 * set for each one
 */
public class ElmCodeResolvingValuesetService implements IValuesetService {
    private IOmopRepositoryService omopService;

    private Library library;

    private int conceptSetId;

    private Map<PhemaCode, String> codeOidMap;

    public ElmCodeResolvingValuesetService(IOmopRepositoryService omopService, Library library) {
        this.omopService = omopService;
        this.library = library;
        this.conceptSetId = 0;
        this.codeOidMap = new HashMap<>();
    }

    private String codeSystemNameFromRef(CodeSystemRef codeSystemRef) throws ValuesetServiceException {
        Optional<CodeSystemDef> system = library
            .getCodeSystems()
            .getDef()
            .stream()
            .filter(cd -> cd.getName().equals(codeSystemRef.getName()))
            .findFirst();

        if (system.isPresent()) {
            return system.get().getId();
        } else {
            throw new ValuesetServiceException(String.format("Code system %s not found", codeSystemRef.getName()));
        }
    }

    private PhemaCode phemaCodeFromCodeDef(CodeDef codeDef) throws ValuesetServiceException {
        PhemaCode phemaCode = new PhemaCode();

        phemaCode.setCode(codeDef.getId());
        phemaCode.setCodeSystem(codeSystemNameFromRef(codeDef.getCodeSystem()));

        return phemaCode;
    }

    private List<PhemaCode> translateCodes() throws ValuesetServiceException {
        List<PhemaCode> translatedCodes = new ArrayList<>();

        PhemaCode code = null;
        PhemaCode translated = null;
        for (CodeDef codeDef : library.getCodes().getDef()) {
            code = phemaCodeFromCodeDef(codeDef);

            try {
                if (PhemaVocabularyTranslator.translationSupportedForCodeSystem(code.getCodeSystem())) {
                    translated = PhemaVocabularyTranslator.translateCode(code);
                } else {
                    translated = code;
                }

                if (!translatedCodes.contains(translated)) {
                    translatedCodes.add(translated);
                    codeOidMap.put(translated, codeDef.getName());
                }
            } catch (PhemaTranslationException pte) {
                throw new ValuesetServiceException("Error translating ELM code", pte);
            }
        }

        return translatedCodes;
    }

    private Concept getConcept(PhemaCode code) throws ValuesetServiceException {
        List<Concept> concept = null;
        try {
            concept = omopService.vocabularySearch(code.getCode(), code.getCodeSystem());
        } catch (OmopRepositoryException e) {
            throw new ValuesetServiceException("Error searching for concept", e);
        }

        // Filter for only exact matches (this isn't possible current with the OHDSI WebAPI)
        concept = concept.stream()
            .filter(c -> c.conceptCode.equals(code.getCode()))
            .collect(Collectors.toList());

        if (concept.size() > 1) {
            // The concept code is not specific enough
            throw new ValuesetServiceException(String.format("Concept code %s does not specify a single concept in code system %s", code.getCode(), code.getCodeSystem()));
        } else if (concept.isEmpty()) {
            // The code is missing in the OMOP instance
            throw new ValuesetServiceException(String.format("Concept code %s does not exist in OMOP instance", code.getCode()));
        } else {
            return concept.get(0);
        }
    }

    private PhemaConceptSet buildConceptSet(Concept concept, String oid) {
        PhemaConceptSet phemaConceptSet = new PhemaConceptSet();

        phemaConceptSet.setOid(oid);
        phemaConceptSet.name = oid;

        ConceptSetExpression.ConceptSetItem item = new ConceptSetExpression.ConceptSetItem();
        item.concept = concept;

        ConceptSetExpression conceptSetExpression = new ConceptSetExpression();
        conceptSetExpression.items = new ConceptSetExpression.ConceptSetItem[]{item};

        phemaConceptSet.expression = conceptSetExpression;
        phemaConceptSet.id = conceptSetId++;

        return phemaConceptSet;
    }

    @Override
    public PhemaConceptSetList getConceptSetList() throws ValuesetServiceException {
        PhemaConceptSetList phemaConceptSetList = new PhemaConceptSetList();

        List<PhemaCode> codes = translateCodes();

        Concept concept = null;
        for (PhemaCode code : codes) {
            concept = getConcept(code);
            phemaConceptSetList.addConceptSet(buildConceptSet(concept, codeOidMap.get(code)));
        }

        return phemaConceptSetList;
    }

    @Override
    public List<PhemaConceptSet> getConceptSets() throws ValuesetServiceException {
        return getConceptSetList().getConceptSets();
    }
}
