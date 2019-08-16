package edu.phema.elm_to_omop.model.omop;

public class CriteriaListEntry {
    private Criteria criteria;
    private StartWindow startWindow;
    private Occurrence occurrence;
    private VisitOccurrence visitOcc;

    public CriteriaListEntry() {
    }

    public CriteriaListEntry(VisitOccurrence visitOcc) {
        this.visitOcc = visitOcc;
    }

    public VisitOccurrence getVisitOcc() {
        return visitOcc;
    }

    public void setVisitOcc(VisitOccurrence visitOcc) {
        this.visitOcc = visitOcc;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public StartWindow getStartWindow() {
        return startWindow;
    }

    public void setStartWindow(StartWindow startWindow) {
        this.startWindow = startWindow;
    }

    public Occurrence getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Occurrence occurrence) {
        this.occurrence = occurrence;
    }
}
