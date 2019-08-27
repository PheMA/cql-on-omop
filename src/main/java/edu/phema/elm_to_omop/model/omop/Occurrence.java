package edu.phema.elm_to_omop.model.omop;

public class Occurrence {

    public static class Type {
        public static final String Exactly = "0";
        public static final String AtMost = "1";
        public static final String AtLeast = "2";
    }

    private String type;
    private String count;

    public Occurrence(String type, String count) {
        super();
        this.type = type;
        this.count = count;
    }

    public Occurrence(String type, int count) {
        super();
        this.type = type;
        setCount(count);
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

    public void setCount(int count) {
        this.count = Integer.toString(count);
    }

    public String getJsonFragment() {
        return String.format("{ \"Type\": %s, \"Count\": %s }",
            type, count);
    }
}
