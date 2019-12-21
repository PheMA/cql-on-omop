package edu.phema.elm_to_omop.translate.criteria.correlation;

import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;

@FunctionalInterface
public interface CorrelatedQueryCorelatedCriteriaGeneratorFunction<T, U, R> {
    R apply(T t, U u) throws PhemaTranslationException, CorrelationException;
}
