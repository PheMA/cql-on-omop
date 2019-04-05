package edu.phema.elm_to_omop.model_omop;

public class InclusionRules {
    
    private static String name;
    private static InclusionExpression expression;
    
    public InclusionRules(String name, InclusionExpression expression) {
        super();
        this.name = name;
        this.expression = expression;
    }

    public static String getName() {
        return name;
    }

    public static InclusionExpression getExpression() {
        return expression;
    }

    public static void setName(String name) {
        InclusionRules.name = name;
    }

    public static void setExpression(InclusionExpression expression) {
        InclusionRules.expression = expression;
    }
    
    public String getInclusionRulesJson()  {
        String condOcc = expression.getInclusionCriteriaList().getCriteria().getConditionOccurrence().getCodesetId();
        String startCoeff = expression.getInclusionCriteriaList().getStartWindow().getStart().getCoeff();
        String endCoeff = expression.getInclusionCriteriaList().getStartWindow().getEnd().getCoeff();
        String occType =  expression.getInclusionCriteriaList().getOccurrence().getType();
        String occCount = expression.getInclusionCriteriaList().getOccurrence().getCount();
        
        return "\"InclusionRules\": [ {  "
                + "\"name\": " +name +",  "
                + "\"expression\": {  "
                    + "\"Type\": " +expression.getType() +",  "
                + "\"CriteriaList\": [  { "
                    + "\"Criteria\": {  "
                        + "\"ConditionOccurrence\": {  \"CodesetId\": " +condOcc +" } }, "
                    + "\"StartWindow\": { \"Start\": {  \"Coeff\": " +startCoeff +" },  \"End\": { \"Coeff\": " +endCoeff +" } }, "
                    + "\"Occurrence\": { \"Type\": " +occType +", \"Count\": " +occCount +" } } ],         "
                + "\"DemographicCriteriaList\": [],  "
                + "\"Groups\": []  "
                + "} } ],  ";
    }
    
}
