package edu.phema.elm_to_omop.model_omop;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "concept" })
public class Items {

    private Concept concept;
    //private boolean descendents;

    public Items() {
        super();
    }
    
    public Concept getConcept() {
        return concept;
    }
//    public boolean isDescendents() {
//        return descendents;
//    }
    public void setConcept(Concept concept) {
        this.concept = concept;
    }
//    public void setDescendents(boolean descendents) {
//        this.descendents = descendents;
//    }
    
}
