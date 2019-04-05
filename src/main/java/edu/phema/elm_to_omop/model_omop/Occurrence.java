package edu.phema.elm_to_omop.model_omop;

public class Occurrence {

    private String type;
    private String count;
    
    public Occurrence(String type, String count) {
        super();
        this.type = type;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public String getCount() {
        return count;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCount(String count) {
        this.count = count;
    }
    
    
}
