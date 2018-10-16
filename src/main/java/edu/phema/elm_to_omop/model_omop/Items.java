package edu.phema.elm_to_omop.model_omop;

import java.util.ArrayList;

public class Items {

    private ArrayList<Concept> concepts;
    private boolean descendents;
    
    
    public Items() {
        super();
        concepts = new ArrayList<Concept>();
    }
    
    public ArrayList<Concept> getConcepts() {
        return concepts;
    }
    public boolean isDescendents() {
        return descendents;
    }
    public void setConcepts(ArrayList<Concept> concepts) {
        this.concepts = concepts;
    }
    public void setDescendents(boolean descendents) {
        this.descendents = descendents;
    }
    public void add(Concept concept)  {
        this.concepts.add(concept);
    }
    
    
    
}
