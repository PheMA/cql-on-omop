package edu.phema.elm_to_omop.model_omop;

import java.util.ArrayList;

public class ConceptSets {

    private int id;
    private String name;
    private Expression expression;
    
    
    public ConceptSets() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public ConceptSets(int id, String name, Expression expression) {
        super();
        this.id = id;
        this.name = name;
        this.expression = expression;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Expression getExpression() {
        return expression;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
    public String getConceptSetsJson()  {
        String conSet = "";

        conSet = "\"ConceptSets\": [ { ";
        conSet = conSet +"\"id\": " +id +", ";
        conSet = conSet +"\"name\": \"" +name +"\", ";
        conSet = conSet +"\"expression\": { \"items\": [ ";
        
        ArrayList<Items> items = new ArrayList<Items>();
        items = expression.getItems();
        
        for (Items item : items) {
            Concept concept = item.getConcept();
            
            conSet = conSet +"{ \"concept\": { ";
            if(concept.getId()!=null)  
                conSet = conSet +"\"CONCEPT_ID\": " +concept.getId() +", ";
            if(concept.getName()!=null) 
            conSet = conSet +"\"CONCEPT_NAME\": \"" +concept.getName() +"\", ";
            if(concept.getStandardConcept()!=null) 
                conSet = conSet +"\"STANDARD_CONCEPT\": \"" +concept.getStandardConcept() +"\", ";
            if(concept.getStandardConceptCaption()!=null) 
                conSet = conSet +"\"STANDARD_CONCEPT_CAPTION\": \"" +concept.getStandardConceptCaption() +"\", ";
            if(concept.getInvalidReason()!=null) 
                conSet = conSet +"\"INVALID_REASON\": \"" +concept.getInvalidReason() +"\", ";
            if(concept.getInvalidReasonCaption()!=null) 
                conSet = conSet +"\"INVALID_REASON_CAPTION\": \"" +concept.getInvalidReasonCaption() +"\", ";
            if(concept.getConceptCode()!=null) 
                conSet = conSet +"\"CONCEPT_CODE\": \"" +concept.getConceptCode() +"\", ";
            if(concept.getDomainId()!=null) 
                conSet = conSet +"\"DOMAIN_ID\": \"" +concept.getDomainId() +"\", ";
            if(concept.getVocabularyId()!=null) 
                conSet = conSet +"\"VOCABULARY_ID\": \"" +concept.getVocabularyId() +"\", ";
            if(concept.getConceptClassId()!=null) 
                conSet = conSet +"\"CONCEPT_CLASS_ID\": \"" +concept.getConceptClassId() +"\" ";
            else 
                conSet = conSet.substring(0, conSet.length()-2);  
            conSet = conSet +"} }, ";
        }
        conSet = conSet.substring(0, conSet.length()-2);
        conSet = conSet +"] } } ], ";  // closing out the items array, expression and ConceptSets array
        
        return conSet;
    }
    
}
