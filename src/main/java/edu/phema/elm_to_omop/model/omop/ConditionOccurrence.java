package edu.phema.elm_to_omop.model.omop;

public class ConditionOccurrence {

    private String codesetId;

    public ConditionOccurrence(String codesetId) {
        super();
        this.codesetId = codesetId;
    }

    public String getCodesetId() {
        return codesetId;
    }

    public void setCodesetId(String codesetId) {
        this.codesetId = codesetId;
    }


}
