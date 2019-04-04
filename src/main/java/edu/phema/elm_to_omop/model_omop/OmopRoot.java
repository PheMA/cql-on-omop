package edu.phema.elm_to_omop.model_omop;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "description", "expressionType", "expression" })

public class OmopRoot {
    String name = "Diabetes";
    String description = "none";
    String expressionType = "SIMPLE_EXPRESSION";
    
    ExpressionDefinition expression;
    
    public OmopRoot() {
        super();
        // TODO Auto-generated constructor stub
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExpressionType() {
        return expressionType;
    }

    public ExpressionDefinition getExpression() {
        return expression;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExpressionType(String expressionType) {
        this.expressionType = expressionType;
    }

    public void setExpression(ExpressionDefinition expression) {
        this.expression = expression;
    }
    
    
}
