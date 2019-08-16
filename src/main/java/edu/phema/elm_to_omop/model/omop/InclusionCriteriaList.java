package edu.phema.elm_to_omop.model.omop;

public class InclusionCriteriaList {

    private Criteria crit;
    private StartWindow start;
    private Occurrence occ;


    public InclusionCriteriaList(Criteria crit, StartWindow start, Occurrence occ) {
        super();
        this.crit = crit;
        this.start = start;
        this.occ = occ;
    }

    public Criteria getCriteria() {
        return crit;
    }
    public StartWindow getStartWindow() {
        return start;
    }
    public Occurrence getOccurrence() {
        return occ;
    }

    public void setCriteria(Criteria crit) {
        this.crit = crit;
    }
    public void setStartWindow(StartWindow start) {
        this.start = start;
    }
    public void setOccurrence(Occurrence occ) {
        this.occ = occ;
    }


}
