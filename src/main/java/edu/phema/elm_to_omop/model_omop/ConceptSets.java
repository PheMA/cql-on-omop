package edu.phema.elm_to_omop.model_omop;

public class ConceptSets {

    private String id;
    private String name;
    private Expression expression;
    
    
    public ConceptSets() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public ConceptSets(String id, String name, Expression expression) {
        super();
        this.id = id;
        this.name = name;
        this.expression = expression;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Expression getExpression() {
        return expression;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
    
}
