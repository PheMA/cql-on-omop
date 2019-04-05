package edu.phema.elm_to_omop.model_omop;

public class QualifiedLimit {

    private String type;

    public QualifiedLimit(String type) {
        super();
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getQualifiedLimitJson()  {
        return "\"QualifiedLimit\": {  \"Type\": " +type +" },  ";
    }
}
