package edu.phema.elm_to_omop.model_omop;

import java.util.ArrayList;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "ConceptSets", "primaryCriteria", "qualifiedLimit", "expressionLimit", "inclusionRules", 
                        "censoringCriteria", "collapseSettings", "censorWindow" })

/**
 * The expression part of the statement is the phenotype query
 */
public class ExpressionDefinition {
    
    @JsonProperty("\"conceptSets\"")
    private ArrayList<ConceptSets> conceptSets;
    
    String expression = "";
    
    public ExpressionDefinition() {
        super();
    }
    
    public String getExpression()  {
        return expression;
    }
    
    /**
     * There were values for the conceptSets in simple example.
     * Other parts of the expression were hard coded for proof of concept
     * As get more complex examples, the hard coded values will be better understood and replaced
     */
    public void setExpression(ConceptSets cs)  {
        expression = "{ " 
                + getConceptSets(cs)  
                + getPrimaryCriteria()
                + getQualifiedLimit()
                + getExpressionLimit()
                + getInclusionRules()
                + getCensoringCriteria()
                + getCollapseSettings()
                + getCensorWindow()
                + " }";
    }

    public String getConceptSets(ConceptSets cs)  {
        return cs.getConceptSetsJson();
    }
    
    
    // TODO: everything under here did not have values in the simple example.  Default values were hard coded.

    private String getPrimaryCriteria()  {
        String visitOcc = "";
        VisitOccurrence vo = new VisitOccurrence(visitOcc);
        CriteriaList cl = new CriteriaList(vo);
        
        String priorDays = "0";
        String postDays = "0";
        ObservationWindow ow = new ObservationWindow(priorDays, postDays);
        
        String type = " \"First\" ";
        PrimaryCriteriaLimit pcl = new PrimaryCriteriaLimit(type);
        
        PrimaryCriteria pc = new PrimaryCriteria(cl, ow, pcl);
        return pc.getPrimaryCriteriaJson();
    }
    
    private String getQualifiedLimit()  {
        String type = " \"First\" ";
        QualifiedLimit ql = new QualifiedLimit(type);
        
        return ql.getQualifiedLimitJson();
    }
    
    private String getExpressionLimit()   {
        String type = " \"First\" ";
        ExpressionLimit el = new ExpressionLimit(type);
        
        return el.getExpressionLimitJson();
    }
    
    private String getInclusionRules()  {
        String name = " \"Diabetes\" ";     
        String type = " \"ALL\" ";          
        String codesetId = "0";
        String startCoeff = "-1";
        String endCoeff = "1";
        String occType = "2";
        String occCount = "1";
        
        Start start = new Start(startCoeff);
        End end = new End(endCoeff);
        StartWindow startWin = new StartWindow(start, end);
        
        ConditionOccurrence conditionOccurrence = new ConditionOccurrence(codesetId);
        Criteria crit = new Criteria(conditionOccurrence);
        
        Occurrence occ = new Occurrence(occType, occCount);
        
        InclusionCriteriaList icl = new InclusionCriteriaList(crit, startWin, occ);
        InclusionDemographic id = new InclusionDemographic();
        InclusionGroups ig = new InclusionGroups();
        
        InclusionExpression ie = new InclusionExpression(type, icl, id, ig);
        
        InclusionRules ir = new InclusionRules(name, ie);

        return ir.getInclusionRulesJson() ;
    }
    
    private String getCensoringCriteria()  {
        CensoringCriteria cc = new CensoringCriteria();
        
        return cc.getCensoringCriteriaJson();
    }
    
    private String getCollapseSettings()  {
        String type = " \"ERA\" ";
        String eraPad = "0";
        CollapseSettings cs = new CollapseSettings(type, eraPad);
        
        return cs.getCollapseSettingJson();
    }
    
    private String getCensorWindow()  {
        CensorWindow cw = new CensorWindow();
        
        return cw.getCensorWindowJson();
    }
}
