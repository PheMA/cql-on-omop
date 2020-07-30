package edu.phema.elm_to_omop.translate.criteria.correlation;

import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;

import java.util.HashMap;
import java.util.Map;

public class CorrelationConstants {

    /**
     * Represents the QUICK resources we support
     */
    public enum QuickResourceType {
        ENCOUNTER("Encounter"),
        CONDITION("Condition");

        private final String quickResource;

        QuickResourceType(final String typeString) {
            this.quickResource = typeString;
        }

        @Override
        public String toString() {
            return quickResource;
        }

        public static QuickResourceType create(String quickResourceName) throws CorrelationException {
            switch (quickResourceName.toUpperCase()) {
                case "ENCOUNTER":
                    return QuickResourceType.ENCOUNTER;
                case "CONDITION":
                    return QuickResourceType.CONDITION;
                default:
                    throw new CorrelationException(String.format("%s resource not supported for correlation", quickResourceName));
            }
        }
    }

    /**
     * Represents the QUICK resource attributes we support
     */
    public enum QuickResourceAttribute {
        ID("id"),
        ONSET_DATE_TIME("onsetDateTime"),
        ENCOUNTER("encounter");

        private final String quickResourceAtt;

        QuickResourceAttribute(final String typeString) {
            this.quickResourceAtt = typeString;
        }

        @Override
        public String toString() {
            return quickResourceAtt;
        }

        public static QuickResourceAttribute create(String quickResourceAttributeName) throws CorrelationException {
            switch (quickResourceAttributeName.toUpperCase()) {
                case "ID":
                    return QuickResourceAttribute.ID;
                case "ONSETDATETIME":
                    return QuickResourceAttribute.ONSET_DATE_TIME;
                case "ENCOUNTER":
                    return QuickResourceAttribute.ENCOUNTER;
                default:
                    throw new CorrelationException(String.format("%s resource not supported for correlation", quickResourceAttributeName));
            }
        }
    }

    /**
     * Represents the Circe criteria we support
     */
    public enum CirceCriteria {
        // Domain criteria
        CONDITION_OCCURRENCE,
        PROCEDURE_OCCURRENCE,

        // Correlation criteria
        CORRLATED_CRITERIA
    }

    /**
     * Represents the Circe criteria attributes we support
     */
    public enum CirceCriteriaAttribute {
        RESTRICT_VISIT,
        CODESET_ID
    }

    /**
     * Contains the QUICK resource-attribute pairs for which we support correlation, along with the translation
     * generator function
     */
    public static Map<Correlation, CorrelatedQueryCorelatedCriteriaGeneratorFunction<Correlation, PhemaElmToOmopTranslatorContext, CorelatedCriteria>> generators = new HashMap<>();

    static {
        try {
            // Functions for generating correlated criteria
            // TODO: Condition.onsetDateTime <temporal operator> Condition.onsetDateTime, and others
            generators.put(Correlation.from("Encounter", "id", "Condition", "encounter"), CorrelatedQueryCorelatedCriteriaGenerator::encounterCondition);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
