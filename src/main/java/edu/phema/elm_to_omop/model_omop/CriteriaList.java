package edu.phema.elm_to_omop.model_omop;

public class CriteriaList {

    private ConditionOccurrence condOcc;

    public CriteriaList() {
        super();
        // TODO Auto-generated constructor stub
    }

    public CriteriaList(ConditionOccurrence condOcc) {
        super();
        this.condOcc = condOcc;
    }

    public ConditionOccurrence getCondOcc() {
        return condOcc;
    }

    public void setCondOcc(ConditionOccurrence condOcc) {
        this.condOcc = condOcc;
    }
    
    
}
