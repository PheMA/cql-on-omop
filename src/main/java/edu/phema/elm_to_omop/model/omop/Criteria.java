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


    public String getJsonFragment() {
        return String.format("{ \"ConditionOccurrence\": {  \"CodesetId\": " + conditionOccurrence.getCodesetId() + " } } ");
    }
}
