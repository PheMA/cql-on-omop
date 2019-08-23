package edu.phema.elm_to_omop.model.omop;

import java.util.ArrayList;
import java.util.List;

/**
 * The expression part of the statement is the phenotype query
 */
public class ExpressionDefinition {

    private String expression = "";
    private List<InclusionRule> inclusionRules = new ArrayList<InclusionRule>();
    private List<ConceptSet> conceptSets = new ArrayList<ConceptSet>();

    public ExpressionDefinition() {
        super();
    }

    public String getExpression()  {
        return expression;
    }

    public List<ConceptSet> getConceptSets() { return conceptSets; }

    public void setConceptSets(List<ConceptSet> conceptSets) {
        this.conceptSets = conceptSets;
    }

    public void addConceptSet(ConceptSet conceptSet) { this.conceptSets.add(conceptSet); }

    public List<InclusionRule> getInclusionRules() {
        return inclusionRules;
    }

    public void setInclusionRules(List<InclusionRule> inclusionRules) {
        this.inclusionRules = inclusionRules;
    }

    public void addInclusionRule(InclusionRule inclusionRule) { this.inclusionRules.add(inclusionRule); }

    /**
     * There were values for the conceptSets in simple example.
     * Other parts of the expression were hard coded for proof of concept
     * As get more complex examples, the hard coded values will be better understood and replaced
     */
    private void createExpression() throws Exception {
        expression = "{ "
                + formatConceptSets()
                + getPrimaryCriteria()
                + getQualifiedLimit()
                + getExpressionLimit()
                + formatInclusionRules()
                + getCensoringCriteria()
                + getCollapseSettings()
                + getCensorWindow()
                + " }";
    }

    public String getJsonFragment() throws Exception {
        createExpression();
        return expression;
    }

    /**
     * Take our concept sets collection and generate the JSON fragment
     * @return
     */
    private String formatConceptSets() {
        // TODO - Duplicate code (see fromatInclusionRules), let's refactor this!
        StringBuilder builder = new StringBuilder();
        builder.append("\"ConceptSets\": [");
        int conceptSetCount = conceptSets.size();
        for (int index = 0; index < conceptSetCount; index++) {
            ConceptSet conceptSet = conceptSets.get(index);
            builder.append(conceptSet.getJsonFragment());
            if (index < (conceptSetCount - 1)) {
                builder.append(", ");
            }
        }
        builder.append("],");
        return builder.toString();
    }

    /**
     * Take our inclusion rules collection and generate the JSON fragment
     * @return
     */
    private String formatInclusionRules() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("\"InclusionRules\": [");
        int ruleCount = inclusionRules.size();
        for (int index = 0; index < ruleCount; index++) {
            InclusionRule rule = inclusionRules.get(index);
            builder.append(rule.getJsonFragment());
            if (index < (ruleCount - 1)) {
                builder.append(", ");
            }
        }
        builder.append("],");
        return builder.toString();
    }

    // TODO: everything under here did not have values in the simple example.  Default values were hard coded.

    private String getPrimaryCriteria() throws Exception {
        String visitOcc = "";
        VisitOccurrence vo = new VisitOccurrence(visitOcc);
        CriteriaList cl = new CriteriaList();
        cl.addEntry(new CriteriaListEntry(vo));

        String priorDays = "0";
        String postDays = "0";
        ObservationWindow ow = new ObservationWindow(priorDays, postDays);

        String type = " \"First\" ";
        PrimaryCriteriaLimit pcl = new PrimaryCriteriaLimit(type);

        PrimaryCriteria pc = new PrimaryCriteria(cl, ow, pcl);
        return "\"PrimaryCriteria\": " + pc.getJsonFragment() + ", ";
    }

    private String getQualifiedLimit()  {
        String type = " \"First\" ";
        QualifiedLimit ql = new QualifiedLimit(type);

        return ql.getQualifiedLimitJson();
    }

    private String getExpressionLimit()   {
        String type = " \"First\" ";
        ExpressionLimit el = new ExpressionLimit(type);

        return el.getExpressionLimitJson();
    }

    private String getCensoringCriteria()  {
        CensoringCriteria cc = new CensoringCriteria();

        return cc.getCensoringCriteriaJson();
    }

    private String getCollapseSettings()  {
        String type = " \"ERA\" ";
        String eraPad = "0";
        CollapseSettings cs = new CollapseSettings(type, eraPad);

        return cs.getCollapseSettingJson();
    }

    private String getCensorWindow()  {
        CensorWindow cw = new CensorWindow();

        return cw.getCensorWindowJson();
    }
}
