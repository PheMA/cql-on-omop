package edu.phema.elm_to_omop.model.omop;

public class PrimaryCriteriaLimit {
    private String type;

    public PrimaryCriteriaLimit(String type) {
        super();
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJsonFragment() {
        return String.format("{ \"Type\": %s }", type);
    }
}
