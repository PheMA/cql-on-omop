package edu.phema.elm_to_omop.model.omop;

public class ExpressionLimit {

    private String type;

    public ExpressionLimit(String type) {
        super();
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpressionLimitJson()   {
        return "\"ExpressionLimit\": {  \"Type\": " +type +" },  ";
    }

}
