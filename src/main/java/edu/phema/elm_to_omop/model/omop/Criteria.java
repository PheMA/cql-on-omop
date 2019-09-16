package edu.phema.elm_to_omop.model.omop;

public class Criteria {

    private ConditionOccurrence conditionOccurrence;

    public Criteria(ConditionOccurrence conditionOccurrence) {
        super();
        this.conditionOccurrence = conditionOccurrence;
    }

    public ConditionOccurrence getConditionOccurrence() {
        return conditionOccurrence;
    }

    public void setConditionOccurrence(ConditionOccurrence conditionOccurrence) {
        this.conditionOccurrence = conditionOccurrence;
    }


    public String getJsonFragment() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        if (conditionOccurrence != null) {
            builder.append(conditionOccurrence.getJsonFragment());
        }
        else {
            throw new Exception("Invalid criteria - has no specific details included");
        }
        builder.append(" }");
        return builder.toString();
    }
}
