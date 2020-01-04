package edu.phema.elm_to_omop.vocabulary.translate.map;

import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.vocabulary.phema.PhemaCode;

/**
 * Interface to implement for vocabulary mapping
 */
public interface IVocabularyMap {
    /**
     * Used to determine which source vocabulary this mapping supports
     *
     * @return The source vocabulary
     */
    public String sourceVocabulary();

    /**
     * Translates a code from the source vocabulary to the target vocabulary
     *
     * @param code The code in the source vocabulary to translate
     * @return The translated code
     * @throws PhemaTranslationException
     */
    public PhemaCode translate(PhemaCode code) throws PhemaTranslationException;
}
