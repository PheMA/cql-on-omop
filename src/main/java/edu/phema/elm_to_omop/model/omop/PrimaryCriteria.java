package edu.phema.elm_to_omop.model.omop;

public class PrimaryCriteria {

    private CriteriaList criteriaList;
    private ObservationWindow observationWindow;
    private PrimaryCriteriaLimit primaryCriteriaLimit;

    public PrimaryCriteria(CriteriaList critList, ObservationWindow obWin, PrimaryCriteriaLimit primCritLimit) {
        super();
        this.criteriaList = critList;
        this.observationWindow = obWin;
        this.primaryCriteriaLimit = primCritLimit;
    }


    public CriteriaList getCritList() {
        return criteriaList;
    }

    public ObservationWindow getObWin() {
        return observationWindow;
    }

    public PrimaryCriteriaLimit getPrimCritLimit() {
        return primaryCriteriaLimit;
    }

    public void setCritList(CriteriaList critList) {
        this.criteriaList = critList;
    }


    public void setObWin(ObservationWindow obWin) {
        this.observationWindow = obWin;
    }

    public void setPrimCritLimit(PrimaryCriteriaLimit primCritLimit) {
        this.primaryCriteriaLimit = primCritLimit;
    }

    public String getJsonFragment() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(criteriaList.getJsonFragment(CriteriaListEntry.PrimaryCriteriaFormat));
        builder.append(", \"ObservationWindow\": ");
        builder.append(observationWindow.getJsonFragment());
        builder.append(", \"PrimaryCriteriaLimit\": ");
        builder.append(primaryCriteriaLimit.getJsonFragment());
        builder.append("}");
        return builder.toString();
    }


}
