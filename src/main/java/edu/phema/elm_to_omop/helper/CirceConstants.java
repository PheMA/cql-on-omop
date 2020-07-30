package edu.phema.elm_to_omop.helper;

/**
 * Various enums that should in theory
 * exist in the Circe library
 */
public class CirceConstants {
    public enum CriteriaGroupType {
        ANY("ANY"),
        ALL("ALL"),
        AT_LEAST("AT_LEAST"),
        AT_MOST("AT_MOST");

        private final String typeString;

        CriteriaGroupType(final String typeString) {
            this.typeString = typeString;
        }

        @Override
        public String toString() {
            return typeString;
        }
    }

    public enum NumericRangeOperator {
        LESS_THAN("lt"),
        LESS_THAN_OR_EQUAL("lte"),
        EQUAL("eq"),
        NOT_EQUAL("!eq"),
        GREATER_THAN("gt"),
        GREATER_THAN_OR_EQUAL("gte");

        private final String opString;

        NumericRangeOperator(final String opString) {
            this.opString = opString;
        }

        @Override
        public String toString() {
            return opString;
        }
    }
}
