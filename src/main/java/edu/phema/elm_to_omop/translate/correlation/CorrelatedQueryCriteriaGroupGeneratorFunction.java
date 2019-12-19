package edu.phema.elm_to_omop.translate.correlation;

import edu.phema.elm_to_omop.translate.PhemaTranslationException;

@FunctionalInterface
public interface CorrelatedQueryCriteriaGroupGeneratorFunction<T, U, R> {
    R apply(T t, U u) throws PhemaTranslationException, CorrelationException;
}
