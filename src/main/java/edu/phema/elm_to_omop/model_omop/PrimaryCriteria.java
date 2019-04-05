package edu.phema.elm_to_omop.model_omop;

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

    public String getPrimaryCriteriaJson()  {
        return "\"PrimaryCriteria\": {  "
                + "\"CriteriaList\": [  {  "
                    + " \"VisitOccurrence\": { " +criteriaList.getVisitOcc().getCondesetId() + "}  }  ], "
                + "\"ObservationWindow\": {  "
                    + "\"PriorDays\": " +observationWindow.getPriorDays() +",  "
                    + "\"PostDays\": " +observationWindow.getPostDays() +" },  "
                + "\"PrimaryCriteriaLimit\": {  "
                    + "\"Type\": " +primaryCriteriaLimit.getType() +"  "
             + "} },   ";
    }
    
    
}
