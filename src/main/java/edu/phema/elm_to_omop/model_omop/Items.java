package edu.phema.elm_to_omop.model_omop;

public class Items {

    private Concept concept;
    private boolean descendents;
    
    
    public Items() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public Concept getConcept() {
        return concept;
    }
    public boolean isDescendents() {
        return descendents;
    }
    public void setConcept(Concept concept) {
        this.concept = concept;
    }
    public void setDescendents(boolean descendents) {
        this.descendents = descendents;
    }
    
    
    
}
