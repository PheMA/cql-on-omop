package edu.phema.elm_to_omop.model.omop;

import java.util.ArrayList;
import java.util.List;

public class InclusionRule {

    private String name;
    private InclusionExpression expression;

    public InclusionRule(String name) {
        super();
        this.name = name;
    }

    public InclusionRule(String name, InclusionExpression expression) {
        super();
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        name = name;
    }

    public InclusionExpression getExpression() {
        return expression;
    }

    public void setExpression(InclusionExpression expression) {
        this.expression = expression;
    }

    public String getJsonFragment() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        if (this.expression != null) {
            builder.append("\"name\": \"" + name + "\",  "
                + "\"expression\": " + expression.getJsonFragment());
        }

        builder.append("}");
        return builder.toString();
    }

}
