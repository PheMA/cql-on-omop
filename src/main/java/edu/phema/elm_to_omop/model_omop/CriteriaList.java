package edu.phema.elm_to_omop.model_omop;

public class CriteriaList {

    private VisitOccurrence visitOcc;

    public CriteriaList(VisitOccurrence visitOcc) {
        super();
        this.visitOcc = visitOcc;
    }

    public VisitOccurrence getVisitOcc() {
        return visitOcc;
    }

    public void setvisitOcc(VisitOccurrence visitOcc) {
        this.visitOcc = visitOcc;
    }
    
    
}
