package edu.phema.elm_to_omop.model.omop;

public class ConditionOccurrence {

    private String codesetId;
    private InclusionExpression correlatedCriteria;

    public ConditionOccurrence(String codesetId) {
        super();
        this.codesetId = codesetId;
    }

    public ConditionOccurrence() {
        super();
    }

    public String getCodesetId() {
        return codesetId;
    }
    public void setCodesetId(String codesetId) {
        this.codesetId = codesetId;
    }

    public InclusionExpression getCorrelatedCriteria() {
        return correlatedCriteria;
    }

    public void setCorrelatedCriteria(InclusionExpression correlatedCriteria) {
        this.correlatedCriteria = correlatedCriteria;
    }

    public String getJsonFragment() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("\"ConditionOccurrence\": { ");
        if (correlatedCriteria != null) {
            builder.append("\"CorrelatedCriteria\": { ");
            builder.append(correlatedCriteria.getJsonFragment());
            builder.append(" }");
        }
        else if (codesetId != null) {
            builder.append("\"CodesetId\": " + codesetId);
        }
        builder.append(" }");
        return builder.toString();
    }
}
