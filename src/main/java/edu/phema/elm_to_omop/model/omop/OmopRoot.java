package edu.phema.elm_to_omop.model.omop;

public class OmopRoot {
    public static final String SIMPLE_EXPRESSION_TYPE = "SIMPLE_EXPRESSION";

    String name = "";
    String description = "";
    String expressionType = SIMPLE_EXPRESSION_TYPE;

    ExpressionDefinition expression;

    public OmopRoot() {
        super();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        if (description == null){
            if (name == null) {
               return "";
            } else {
                return name;
            }
        }
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


    public String getJson() throws Exception {
        return String.format("{ \"name\": \"%s\", \"description\": \"%s\", \"expressionType\": \"%s\", \"expression\": \"%s\" }",
            this.name, getDescription(), this.expressionType, this.expression.getJsonFragment().replaceAll("\\\"", "\\\\\""));
    }
}
