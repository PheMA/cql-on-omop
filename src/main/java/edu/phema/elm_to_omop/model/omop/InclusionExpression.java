package edu.phema.elm_to_omop.model.omop;

public class InclusionExpression {

    public static class Type {
        public static final String Any = "ANY";
        public static final String All = "ALL";
    }
    private String type;
    private CriteriaList criteriaList;
    private InclusionDemographic inclusionDemographic;
    private InclusionGroups ig;

    public InclusionExpression(String type, CriteriaList criteriaList, InclusionDemographic inclusionDemographic, InclusionGroups ig) {
        super();
        this.type = type;
        this.criteriaList = criteriaList;
        this.inclusionDemographic = inclusionDemographic;
        this.ig = ig;
    }

    public String getType() {
        return type;
    }

    public CriteriaList getInclusionCriteriaList() {
        return criteriaList;
    }

    public InclusionDemographic getInclusionDemographic() {
        return inclusionDemographic;
    }

    public InclusionGroups getInclusionGroups() {
        return ig;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setInclusionCriteriaList(CriteriaList criteriaList) {
        this.criteriaList = criteriaList;
    }

    public void setInclusionDemographic(InclusionDemographic inclusionDemographic) {
        this.inclusionDemographic = inclusionDemographic;
    }

    public void setInclusionGroups(InclusionGroups ig) {
        this.ig = ig;
    }

    public String getJsonFragment() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(String.format("\"Type\": \"%s\",  ", this.type));
        builder.append(criteriaList.getJsonFragment(CriteriaListEntry.InclusionCriteriaFormat));
        //builder.append(inclusionDemographic.getJsonFragment());
        builder.append(", \"DemographicCriteriaList\": []  ");
        //builder.append(ig.getJsonFragment());
        builder.append(", \"Groups\": [] ");
        builder.append("}");
        return builder.toString();
    }
}
