package edu.phema.elm_to_omop.model.omop;

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

        public String toString() {
            return typeString;
        }
    }
}
