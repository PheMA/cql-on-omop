package edu.phema.elm_to_omop.model_omop;

public class VisitOccurrence {

    private String  condesetId;

    
    public VisitOccurrence(String condesetId) {
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
