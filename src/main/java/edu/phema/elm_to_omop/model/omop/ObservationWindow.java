package edu.phema.elm_to_omop.model.omop;

public class ObservationWindow {

    private String priorDays;
    private String postDays;

    public ObservationWindow(String priorDays, String postDays) {
        super();
        this.priorDays = priorDays;
        this.postDays = postDays;
    }

    public String getPriorDays() {
        return priorDays;
    }
    public String getPostDays() {
        return postDays;
    }
    public void setPriorDays(String priorDays) {
        this.priorDays = priorDays;
    }
    public void setPostDays(String postDays) {
        this.postDays = postDays;
    }

    public String getJsonFragment() {
        return String.format("{ \"PriorDays\": %s,  \"PostDays\": %s }",
            priorDays, postDays);
    }
}
