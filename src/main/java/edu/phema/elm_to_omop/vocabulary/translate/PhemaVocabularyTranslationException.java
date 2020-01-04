package edu.phema.elm_to_omop.vocabulary.translate;

import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;

public class PhemaVocabularyTranslationException extends PhemaTranslationException {
    public PhemaVocabularyTranslationException(String message) {
        super(message);
    }

    public PhemaVocabularyTranslationException(String message, Throwable cause) {
        super(message, cause);
    }
}
