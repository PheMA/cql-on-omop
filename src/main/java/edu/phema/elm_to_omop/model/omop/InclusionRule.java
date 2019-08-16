package edu.phema.elm_to_omop.model.omop;

import java.util.ArrayList;
import java.util.List;

public class InclusionRule {

    private String name;
    private List<InclusionExpression> expressions;

    public InclusionRule(String name) {
        super();
        this.name = name;
        this.expressions = new ArrayList<InclusionExpression>();
    }

    public InclusionRule(String name, InclusionExpression expression) {
        super();
        this.name = name;
        this.expressions = new ArrayList<InclusionExpression>() {{ add(expression); }};
    }

    public String getName() {
        return name;
    }

    public List<InclusionExpression> getExpressions() {
        return expressions;
    }

    public void setName(String name) {
        name = name;
    }

    public void addExpression(InclusionExpression expression) {
        expressions.add(expression);
    }

    public void setExpressions(List<InclusionExpression> expressions) {
        this.expressions = expressions;
    }

    public String getJsonFragment()  {
        StringBuilder builder = new StringBuilder("\"InclusionRules\": [");

        if (this.expressions != null && this.expressions.size() > 0) {
            int ruleCount = this.expressions.size();
            for (int index = 0; index < ruleCount; index++) {
                InclusionExpression expression = this.expressions.get(index);
                String condOcc = expression.getInclusionCriteriaList().getCriteria().getConditionOccurrence().getCodesetId();
                String startCoeff = expression.getInclusionCriteriaList().getStartWindow().getStart().getCoeff();
                String endCoeff = expression.getInclusionCriteriaList().getStartWindow().getEnd().getCoeff();
                String occType = expression.getInclusionCriteriaList().getOccurrence().getType();
                String occCount = expression.getInclusionCriteriaList().getOccurrence().getCount();
                builder.append("{  "
                    + "\"name\": " + name + ",  "
                    + "\"expression\": {  "
                    + "\"Type\": " + expression.getType() + ",  "
                    + "\"CriteriaList\": [  { "
                    + "\"Criteria\": {  "
                    + "\"ConditionOccurrence\": {  \"CodesetId\": " + condOcc + " } }, "
                    + "\"StartWindow\": { \"Start\": {  \"Coeff\": " + startCoeff + " },  \"End\": { \"Coeff\": " + endCoeff + " } }, "
                    + "\"Occurrence\": { \"Type\": " + occType + ", \"Count\": " + occCount + " } } ],         "
                    + "\"DemographicCriteriaList\": [],  "
                    + "\"Groups\": []  "
                    + "} } ");

                if (index < (ruleCount - 1)) {
                    builder.append(", ");
                }
            }
        }

        builder.append("], ");
        return builder.toString();
    }

}
