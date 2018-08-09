package edu.phema.elm_to_omop.model_omop;

public class PrimaryCriteria {

    private CriteriaList critList;
    private ObservationWindow obWin;
    private PrimaryCriteriaLimit primCritLimit;

    
    public PrimaryCriteria() {
        super();
        // TODO Auto-generated constructor stub
    }


    public PrimaryCriteria(CriteriaList critList, ObservationWindow obWin, PrimaryCriteriaLimit primCritLimit) {
        super();
        this.critList = critList;
        this.obWin = obWin;
        this.primCritLimit = primCritLimit;
    }


    public CriteriaList getCritList() {
        return critList;
    }


    public ObservationWindow getObWin() {
        return obWin;
    }


    public PrimaryCriteriaLimit getPrimCritLimit() {
        return primCritLimit;
    }


    public void setCritList(CriteriaList critList) {
        this.critList = critList;
    }


    public void setObWin(ObservationWindow obWin) {
        this.obWin = obWin;
    }


    public void setPrimCritLimit(PrimaryCriteriaLimit primCritLimit) {
        this.primCritLimit = primCritLimit;
    }


    
    
}
