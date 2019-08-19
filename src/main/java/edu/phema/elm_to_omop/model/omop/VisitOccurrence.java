package edu.phema.elm_to_omop.model.omop;

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

    public String getJsonFragment() throws Exception {
        if (condesetId == null || condesetId == "") {
            return "{}";
        }
        else {
            throw new Exception("Need to implement");
        }
    }
}
