package edu.phema.elm_to_omop.model_omop;

public class ConditionOccurrence {

    private String  condesetId;
    
    public ConditionOccurrence() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ConditionOccurrence(String condesetId) {
        super();
        this.condesetId = condesetId;
    }

    public String getCondesetId() {
        return condesetId;
    }

    public void setCondesetId(String condesetId) {
        this.condesetId = condesetId;
    }

    
}
