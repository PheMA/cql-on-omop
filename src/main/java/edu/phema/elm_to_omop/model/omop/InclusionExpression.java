package edu.phema.elm_to_omop.model.omop;

import java.util.ArrayList;
import java.util.List;

/**
 * The InclusionExpression may live as an entry within the InclusionRules expression entry, and also within the Groups
 * collection of another InclusionExpression
 */
public class InclusionExpression {

    public static class Type {
        public static final String Any = "ANY";
        public static final String All = "ALL";
        public static final String AtLeast = "AT_LEAST";
        public static final String AtMost = "AT_MOST";
    }
    private String type;
    private CriteriaList criteriaList;
    private InclusionDemographic inclusionDemographic;
    private List<InclusionExpression> groups;

    public InclusionExpression(String type, CriteriaList criteriaList, InclusionDemographic inclusionDemographic, List<InclusionExpression> groups) {
        super();
        this.type = type;
        this.criteriaList = criteriaList;
        this.inclusionDemographic = inclusionDemographic;
        this.groups = groups;
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

    public List<InclusionExpression> getInclusionGroups() {
        return groups;
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

    public void setInclusionGroups(List<InclusionExpression> groups) {
        this.groups = groups;
    }

    public void addInclusionGroup(InclusionExpression entry) {
        if (this.groups == null) {
            this.groups = new ArrayList<InclusionExpression>();
        }

        this.groups.add(entry);
    }

    public void addInclusionGroups(List<InclusionExpression> entry) {
        if (this.groups == null) {
            this.groups = new ArrayList<InclusionExpression>();
        }

        this.groups.addAll(entry);
    }

    public String getJsonFragment() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append(String.format("\"Type\": \"%s\",  ", this.type));
        builder.append(criteriaList.getJsonFragment(CriteriaListEntry.InclusionCriteriaFormat));
        //builder.append(inclusionDemographic.getJsonFragment());
        builder.append(", \"DemographicCriteriaList\": []");
        builder.append(", \"Groups\": [");
        if (this.groups != null && this.groups.size() > 0) {
            int numEntries = this.groups.size();
            for (int index = 0; index < this.groups.size(); index++) {
                InclusionExpression entry = this.groups.get(index);
                builder.append(entry.getJsonFragment());
                if (index < (numEntries - 1)) {
                    builder.append(", ");
                }
            }
        }
        builder.append("] ");
        builder.append("}");
        return builder.toString();
    }
}
