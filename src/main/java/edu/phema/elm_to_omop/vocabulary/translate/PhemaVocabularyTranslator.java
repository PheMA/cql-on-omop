package edu.phema.elm_to_omop.vocabulary.translate;

import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;
import edu.phema.elm_to_omop.vocabulary.translate.map.ActCodeToVisitConceptMap;
import edu.phema.elm_to_omop.vocabulary.translate.map.IVocabularyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Entry point for translating vocabularies. We only support one translation for any given source vocabulary. This is
 * because we always translate to the optimal vocabulary for OMOP. We are not trying to implement a general vocabulary
 * translator.
 */
public class PhemaVocabularyTranslator {
    // Map of supported code systems
    private static Map<String, IVocabularyMap> translators = new HashMap<>();

    static {
        // Translates ActCodes to OMOP visit concepts
        ActCodeToVisitConceptMap actMap = new ActCodeToVisitConceptMap();
        translators.put(actMap.sourceVocabulary(), actMap);
    }

    /**
     * Determines whether translation is supported for a given code system
     *
     * @param codeSystem The code system name
     * @return True if translation is supported, false otherwise
     * @throws PhemaTranslationException
     */
    public static boolean translationSupportedForCodeSystem(String codeSystem) throws PhemaTranslationException {
        return translators.containsKey(codeSystem);
    }

    /**
     * Translates a given code
     *
     * @param code The code to translate
     * @return The translated code
     * @throws PhemaTranslationException
     */
    public static PhemaCode translateCode(PhemaCode code) throws PhemaTranslationException {
        return translators.get(code.getCodeSystem()).translate(code);
    }
}
