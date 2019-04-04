package edu.phema.elm_to_omop.model_omop;

import java.util.ArrayList;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "ConceptSets", "primaryCriteria", "qualifiedLimit", "expressionLimit", "inclusionRules", 
                        "censoringCriteria", "collapseSettings", "censorWindow" })
public class ExpressionDefinition {
    
    String expression = ""; 
    @JsonProperty("\"conceptSets\"")
    private ArrayList<ConceptSets> conceptSets;
    
    public ExpressionDefinition() {
        super();
        expression = getConceptSets();

    }
    
    public String getExpression()  {
        return expression;
    }

    public void setConceptSets(ConceptSets cs)  {

        expression = "{  \"ConceptSets\": [ { ";
        expression = expression +"\"id\": " +cs.getId() +", ";
        expression = expression +"\"name\": \"" +cs.getName() +"\", ";
        expression = expression +"\"expression\": { \"items\": [ ";
        
        ArrayList<Items> items = new ArrayList<Items>();
        items = cs.getExpression().getItems();
        
        for (Items item : items) {
            Concept concept = item.getConcept();
            
            expression = expression +"{ \"concept\": { ";
            if(concept.getId()!=null)  
                expression = expression +"\"CONCEPT_ID\": " +concept.getId() +", ";
            if(concept.getName()!=null) 
            expression = expression +"\"CONCEPT_NAME\": \"" +concept.getName() +"\", ";
            if(concept.getStandardConcept()!=null) 
                expression = expression +"\"STANDARD_CONCEPT\": \"" +concept.getStandardConcept() +"\", ";
            if(concept.getStandardConceptCaption()!=null) 
                expression = expression +"\"STANDARD_CONCEPT_CAPTION\": \"" +concept.getStandardConceptCaption() +"\", ";
            if(concept.getInvalidReason()!=null) 
                expression = expression +"\"INVALID_REASON\": \"" +concept.getInvalidReason() +"\", ";
            if(concept.getInvalidReasonCaption()!=null) 
                expression = expression +"\"INVALID_REASON_CAPTION\": \"" +concept.getInvalidReasonCaption() +"\", ";
            if(concept.getConceptCode()!=null) 
                expression = expression +"\"CONCEPT_CODE\": \"" +concept.getConceptCode() +"\", ";
            if(concept.getDomainId()!=null) 
                expression = expression +"\"DOMAIN_ID\": \"" +concept.getDomainId() +"\", ";
            if(concept.getVocabularyId()!=null) 
                expression = expression +"\"VOCABULARY_ID\": \"" +concept.getVocabularyId() +"\", ";
            if(concept.getConceptClassId()!=null) 
                expression = expression +"\"CONCEPT_CLASS_ID\": \"" +concept.getConceptClassId() +"\" ";
            else 
                expression = expression.substring(0, expression.length()-2);
            expression = expression +"} }, ";
        }
        expression = expression.substring(0, expression.length()-2);

        expression = expression +"]       }     }   ],   \"PrimaryCriteria\": {     \"CriteriaList\": [       {         \"VisitOccurrence\": {}       }     ],     \"ObservationWindow\": {       \"PriorDays\": 0,       \"PostDays\": 0     },     \"PrimaryCriteriaLimit\": {       \"Type\": \"First\"     }   },   \"QualifiedLimit\": {     \"Type\": \"First\"   },   \"ExpressionLimit\": {     \"Type\": \"First\"   },   \"InclusionRules\": [     {       \"name\": \"Diabetes\",       \"expression\": {         \"Type\": \"ALL\",         \"CriteriaList\": [           { \"Criteria\": {   \"ConditionOccurrence\": {     \"CodesetId\": 0   } }, \"StartWindow\": {   \"Start\": {     \"Coeff\": -1   },   \"End\": {     \"Coeff\": 1   } }, \"Occurrence\": {   \"Type\": 2,   \"Count\": 1 }           }         ],         \"DemographicCriteriaList\": [],         \"Groups\": []       }     }   ],   \"CensoringCriteria\": [],   \"CollapseSettings\": {     \"CollapseType\": \"ERA\",     \"EraPad\": 0   },   \"CensorWindow\": {} }";

    }
    private String getConceptSets() {
        String cs = "";
        
        
        
        return cs;
    }

    
    
}
