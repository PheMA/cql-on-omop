package edu.phema.elm_to_omop.model.phema;

import edu.phema.elm_to_omop.model.PhemaAssumptionException;
import edu.phema.elm_to_omop.model.PhemaNotImplementedException;
import edu.phema.elm_to_omop.model.omop.*;
import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Expression;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.Window;
import org.ohdsi.circe.cohortdefinition.Window.Endpoint;
import org.ohdsi.circe.vocabulary.Concept;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Extension of the CQL Library class to provide utility functions
 */
public class LibraryHelper {
    public static ExpressionDef getExpressionDefByName(Library library, String expressionName) {
        if (library == null || expressionName == null) {
            return null;
        }

        Optional<ExpressionDef> phenotypeExpression = library.getStatements().getDef().stream()
            .filter(x -> expressionName.equals(x.getName()))
            .findFirst();
        return phenotypeExpression.isPresent() ? phenotypeExpression.get() : null;
    }

    /**
     * Given an expression from a CQL/ELM library, generate an OHDSI InclusionRule
     *
     * @param library     The CQL/ELM library that the expression is from
     * @param expression  The expression to translate into a phenotype
     * @param conceptSets The list of value sets (concept sets) related to the CQL/ELM definition
     * @return An InclusionRule that can be included in the OHDSI phenotype definition
     * @throws Exception
     */
    public static List<InclusionRule> generateInclusionRules(Library library, Expression expression, java.util.List<ConceptSet> conceptSets) throws Exception {
        List<InclusionRule> inclusionRules = new ArrayList<InclusionRule>();
        if (isNumericComparison(expression)) {
            inclusionRules.add(generateInclusionRuleForNumericComparison(expression, library, conceptSets));
        } else if (expression instanceof BinaryExpression) {
            inclusionRules.addAll(generateInclusionRulesForBinaryExpression((BinaryExpression) expression, library, conceptSets));
        } else if (expression instanceof Query || expression instanceof Exists) {
            inclusionRules.addAll(generateInclusionRulesForQueryOrExists(expression, library, conceptSets));
        } else if (expression instanceof ExpressionRef) {
            inclusionRules.add(generateInclusionRuleForExpressionRef((ExpressionRef) expression, library, conceptSets));
        } else {
            throw new Exception("The translator is currently unable to generate OHDSI inclusion rules for this type of expression");
        }

        return inclusionRules;
    }

    public static CohortExpression generateCohortExpression(Library library, ExpressionDef expressionDef, List<ConceptSet> conceptSets) throws Exception {
        List<org.ohdsi.circe.cohortdefinition.InclusionRule> inclusionRules = new ArrayList<>();

        Expression expression = expressionDef.getExpression();

        CriteriaGroup criteriaGroup;
        if (isNumericComparison(expression)) {
            criteriaGroup = generateCriteriaGroupForExpression(expression, library, conceptSets);
        } else if (expression instanceof BinaryExpression) {
            criteriaGroup = getCriteriaGroupForBinaryExpression((BinaryExpression) expression, library, conceptSets);
        } else if (expression instanceof Query || expression instanceof Exists) {
            criteriaGroup = generateCriteriaGroupForQueryOrExists(expression, library, conceptSets);
        } else if (expression instanceof ExpressionRef) {
            criteriaGroup = generateCriteriaGroupForExpression(expression, library, conceptSets);
        } else {
            throw new Exception("The translator is currently unable to generate OHDSI inclusion rules for this type of expression");
        }

        // Default to ALL if not set
        if (criteriaGroup.type == null) {
            criteriaGroup.type = CirceConstants.CriteriaGroupType.ALL.toString();
        }

        inclusionRules.add(CirceUtil.inclusionRuleFromCriteriaGroup(expressionDef.getName(), expressionDef.getName(), criteriaGroup));

        CohortExpression cohortExpression = new CohortExpression();

        cohortExpression.primaryCriteria = CirceUtil.getDefaultPrimaryCriteria();

        cohortExpression.title = expressionDef.getName();

        org.ohdsi.circe.cohortdefinition.ConceptSet[] circeConceptSets = new org.ohdsi.circe.cohortdefinition.ConceptSet[conceptSets.size()];
        cohortExpression.conceptSets = conceptSets.stream().map(CirceUtil::convertConceptSetToCirce).collect(Collectors.toList()).toArray(circeConceptSets);
        cohortExpression.inclusionRules = inclusionRules;

        return cohortExpression;
    }

    private static boolean isNumericComparison(Expression expression) {
        return (expression instanceof Greater) ||
            (expression instanceof GreaterOrEqual) ||
            (expression instanceof Equal) ||
            (expression instanceof Less) ||
            (expression instanceof LessOrEqual);
    }

    private static boolean isBooleanExpression(Expression expression) {
        return (expression instanceof Or) ||
            (expression instanceof And) ||
            (expression instanceof Not);
    }

    public static InclusionRule generateInclusionRuleForNumericComparison(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        InclusionRule inclusionRule = new InclusionRule(expression.getClass().getSimpleName());
        CriteriaListEntry entry = generateCriteriaListEntryForExpression(expression, library, conceptSets);
        CriteriaList criteriaList = new CriteriaList() {{
            addEntry(entry);
        }};
        InclusionExpression inclusionExpression = new InclusionExpression(InclusionExpression.Type.All, criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        return inclusionRule;
    }

    /**
     * Helper method to take a Query expression and convert it into an OHDSI InclusionRule.  This is used when the top-level
     * expression for a phenotype is a simple query.
     *
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static List<InclusionRule> generateInclusionRulesForQueryOrExists(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Retrieve retrieveExpression = null;
        InclusionRule correlatedCriteriaRule = null;
        if (expression instanceof Query) {
            Query query = (Query) expression;
            retrieveExpression = getQueryRetrieveExpression(query);
            List<RelationshipClause> relationships = query.getRelationship();
            if (relationships.size() > 0) {
                if (relationships.size() > 1) {
                    throw new PhemaNotImplementedException("The translator is currently only able to handle a single relationship for a data element");
                }

                // Store the alias (needed later)
                String primaryAlias = query.getSource().get(0).getAlias();
                RelationshipClause clause = relationships.get(0);
                if (clause instanceof With) {
                    With with = (With) clause;
                    String secondaryAlias = with.getAlias();
                    // Get an inclusion rule (which contains an inclusion expression, which is what we really need) for the
                    // associated/referenced object for this relationship.
                    List<InclusionRule> linkedRule = generateInclusionRulesForQueryOrExists(with.getExpression(), library, conceptSets);
                    if (linkedRule.size() != 1) {
                        throw new PhemaAssumptionException(String.format("We expected exactly one rule but received %d", linkedRule.size()));
                    }
                    correlatedCriteriaRule = linkedRule.get(0);

                    Expression suchThat = with.getSuchThat();
                    if (!(suchThat instanceof In)) {
                        // TODO: Eventually we'll extend it to BinaryExpression, but need to understand what other types we need
                        throw new PhemaNotImplementedException("The translator is currently only able to process In expressions");
                    }

                    In in = (In) suchThat;
                    List<Expression> operands = in.getOperand();
                    if (operands.size() != 2) {
                        throw new PhemaAssumptionException(String.format("We expected exactly two operands but found %d", operands.size()));
                    }

                    // Now identify the actual temporal constraint, and set that in the window.
                    Property property = (Property) getExpressionOfType(operands, Property.class);
                    Interval interval = (Interval) getExpressionOfType(operands, Interval.class);
                    if (!property.getPath().equals("onsetDateTime")) {
                        throw new PhemaNotImplementedException("The translator is only able to process onsetDateTime temporal relationships");
                    }

                    if (primaryAlias.equals(property.getScope())) {
                        // TODO: This probably happens when we flip the order so that the expression reads "A with B such that A.date 30 days before B.date"
                        // In that case, we need to flip more objects around.  What was outer in CQL needs to become inner in OHDSI because of how
                        // relationships are built.
                        throw new PhemaNotImplementedException("The translator is only able to process simple relationships");
                    }
                    // This happens when the expression reads "A with B such that B.date 30 days before A.date"
                    else if (secondaryAlias.equals(property.getScope())) {
                        // TODO: This is over-fitted to our first use case... need to really evaluate how flexible this is
                        // If the data element is "high", then this is <.  If it's "low", then it's >
                        boolean lessThan = true;
                        Property relatedProperty = (Property) interval.getHigh();
                        BinaryExpression intervalExpression = (BinaryExpression) interval.getLow();
                        if (relatedProperty == null) {
                            lessThan = false;
                            relatedProperty = (Property) interval.getLow();
                            intervalExpression = (BinaryExpression) interval.getHigh();
                        }

                        WindowBoundary start = calculateWindowStart(intervalExpression);
                        correlatedCriteriaRule.getExpression().getInclusionCriteriaList().getEntries().get(0).setStartWindow(new StartWindow(start, new WindowBoundary("1", BigDecimal.ZERO)));
                    }
                } else {
                    throw new PhemaNotImplementedException("The translator is currently only able to process With relationships");
                }
            }
        } else if (expression instanceof Exists) {
            Exists exists = (Exists) expression;
            if (exists.getOperand() instanceof ExpressionRef) {
                return generateInclusionRules(library, exists.getOperand(), conceptSets);
            }
            retrieveExpression = getExistsRetrieveExpression(exists);
        } else if (expression instanceof Retrieve) {
            retrieveExpression = (Retrieve) expression;
        }

        if (retrieveExpression == null) {
            throw new Exception("Unable to generate an inclusion rule for the Query or Exists expression");
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        InclusionRule inclusionRule = new InclusionRule(matchedSet.getName());
        CriteriaListEntry entry = new CriteriaListEntry();
        // TODO - hardcoding for now
        entry.setOccurrence(new Occurrence(Occurrence.Type.AtLeast, "1"));
        entry.setCriteria(generateCriteria(matchedSet, correlatedCriteriaRule));

        CriteriaList criteriaList = new CriteriaList() {{
            addEntry(entry);
        }};
        InclusionExpression inclusionExpression = new InclusionExpression(InclusionExpression.Type.All, criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        return new ArrayList<InclusionRule>() {{
            add(inclusionRule);
        }};
    }

    private static CriteriaGroup generateCriteriaGroupForQueryOrExists(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Retrieve retrieveExpression = null;

        CriteriaGroup corelatedCriteriaGroup = null;
        CorelatedCriteria correlatedCriteria = null;

        if (expression instanceof Query) {
            Query query = (Query) expression;
            retrieveExpression = getQueryRetrieveExpression(query);
            List<RelationshipClause> relationships = query.getRelationship();
            if (relationships.size() > 0) {
                if (relationships.size() > 1) {
                    throw new PhemaNotImplementedException("The translator is currently only able to handle a single relationship for a data element");
                }

                // Store the alias (needed later)
                String primaryAlias = query.getSource().get(0).getAlias();
                RelationshipClause clause = relationships.get(0);
                if (clause instanceof With) {
                    With with = (With) clause;
                    String secondaryAlias = with.getAlias();

                    // Get an inclusion rule (which contains an inclusion expression, which is what we really need) for the
                    // associated/referenced object for this relationship.
                    corelatedCriteriaGroup = generateCriteriaGroupForQueryOrExists(with.getExpression(), library, conceptSets);
                    if (corelatedCriteriaGroup.criteriaList.length != 1) {
                        throw new PhemaAssumptionException(String.format("We expected exactly one rule but received %d", corelatedCriteriaGroup.criteriaList.length));
                    }
                    correlatedCriteria = corelatedCriteriaGroup.criteriaList[0];

                    Expression suchThat = with.getSuchThat();
                    if (!(suchThat instanceof In)) {
                        // TODO: Eventually we'll extend it to BinaryExpression, but need to understand what other types we need
                        throw new PhemaNotImplementedException("The translator is currently only able to process In expressions");
                    }

                    In in = (In) suchThat;
                    List<Expression> operands = in.getOperand();
                    if (operands.size() != 2) {
                        throw new PhemaAssumptionException(String.format("We expected exactly two operands but found %d", operands.size()));
                    }

                    // Now identify the actual temporal constraint, and set that in the window.
                    Property property = (Property) getExpressionOfType(operands, Property.class);
                    Interval interval = (Interval) getExpressionOfType(operands, Interval.class);
                    if (!property.getPath().equals("onsetDateTime")) {
                        throw new PhemaNotImplementedException("The translator is only able to process onsetDateTime temporal relationships");
                    }

                    if (primaryAlias.equals(property.getScope())) {
                        // TODO: This probably happens when we flip the order so that the expression reads "A with B such that A.date 30 days before B.date"
                        // In that case, we need to flip more objects around.  What was outer in CQL needs to become inner in OHDSI because of how
                        // relationships are built.
                        throw new PhemaNotImplementedException("The translator is only able to process simple relationships");
                    }
                    // This happens when the expression reads "A with B such that B.date 30 days before A.date"
                    else if (secondaryAlias.equals(property.getScope())) {
                        // TODO: This is over-fitted to our first use case... need to really evaluate how flexible this is
                        // If the data element is "high", then this is <.  If it's "low", then it's >
                        boolean lessThan = true;
                        Property relatedProperty = (Property) interval.getHigh();
                        BinaryExpression intervalExpression = (BinaryExpression) interval.getLow();
                        if (relatedProperty == null) {
                            lessThan = false;
                            relatedProperty = (Property) interval.getLow();
                            intervalExpression = (BinaryExpression) interval.getHigh();
                        }

                        Endpoint start = calculateStartEndpoint(intervalExpression);

                        Window startWindow = new Window();

                        startWindow.start = calculateStartEndpoint(intervalExpression);

                        startWindow.end = new Window().new Endpoint();
                        startWindow.end.coeff = 1;
                        startWindow.end.days = 0;

                        correlatedCriteria.startWindow = startWindow;
                    }
                } else {
                    throw new PhemaNotImplementedException("The translator is currently only able to process With relationships");
                }
            }
        } else if (expression instanceof Exists) {
            Exists exists = (Exists) expression;
            if (exists.getOperand() instanceof ExpressionRef) {
                return generateCriteriaGroupForExpression(expression, library, conceptSets);
            }
            retrieveExpression = getExistsRetrieveExpression(exists);
        } else if (expression instanceof Retrieve) {
            retrieveExpression = (Retrieve) expression;
        }

        if (retrieveExpression == null) {
            throw new Exception("Unable to generate an inclusion rule for the Query or Exists expression");
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        org.ohdsi.circe.cohortdefinition.Criteria criteria = generateCriteria2(matchedSet, corelatedCriteriaGroup);

        CriteriaGroup criteriaGroup = new CriteriaGroup();

        CorelatedCriteria corelatedCriteria = CirceUtil.defaultCorelatedCriteria();
        corelatedCriteria.criteria = criteria;

        // TODO - hardcoding for now
        org.ohdsi.circe.cohortdefinition.Occurrence occurrence = new org.ohdsi.circe.cohortdefinition.Occurrence();
        occurrence.type = org.ohdsi.circe.cohortdefinition.Occurrence.AT_LEAST;
        occurrence.count = 1;

        corelatedCriteria.occurrence = occurrence;

        criteriaGroup.criteriaList = new CorelatedCriteria[]{corelatedCriteria};

        return criteriaGroup;
    }

    private static Endpoint calculateStartEndpoint(BinaryExpression expression) throws PhemaNotImplementedException, PhemaAssumptionException {
        if (expression instanceof Subtract) {
            Subtract subtract = (Subtract) expression;
            Quantity quantity = (Quantity) getExpressionOfType(subtract.getOperand(), Quantity.class);
            if (quantity == null) {
                throw new PhemaAssumptionException("We expected a quantity to be specified in the relationship, but none was found");
            }

            Endpoint start = new Window().new Endpoint();

            start.coeff = -1;
            start.days = convertToDays(quantity).intValue();

            return start;
        }

        throw new PhemaNotImplementedException("The translator currently only supports subtract operations");
    }

    private static WindowBoundary calculateWindowStart(BinaryExpression expression) throws PhemaNotImplementedException, PhemaAssumptionException {
        if (expression instanceof Subtract) {
            Subtract subtract = (Subtract) expression;
            Quantity quantity = (Quantity) getExpressionOfType(subtract.getOperand(), Quantity.class);
            if (quantity == null) {
                throw new PhemaAssumptionException("We expected a quantity to be specified in the relationship, but none was found");
            }

            WindowBoundary start = new WindowBoundary("-1", convertToDays(quantity));
            return start;
        }

        throw new PhemaNotImplementedException("The translator currently only supports subtract operations");
    }

    static final BigDecimal DAYS_IN_YEAR = BigDecimal.valueOf(365);
    static final BigDecimal DAYS_IN_MONTH = BigDecimal.valueOf(30);

    public static BigDecimal convertToDays(Quantity quantity) throws PhemaAssumptionException, PhemaNotImplementedException {
        if (quantity == null) {
            throw new PhemaAssumptionException("The expected quantity is null");
        }

        if (quantity.getValue() == null || quantity.getUnit() == null || quantity.getUnit().equals("")) {
            throw new PhemaAssumptionException("The quantity must contain both a value and a unit");
        }

        String unit = quantity.getUnit().toLowerCase();
        BigDecimal value = quantity.getValue();
        if (unit.equals("year") || unit.equals("years")) {
            value = value.multiply(DAYS_IN_YEAR);
        } else if (unit.equals("month") || unit.equals("months")) {
            value = value.multiply(DAYS_IN_MONTH);
        } else if (unit.equals("day") || unit.equals("days")) {
            // No conversion needed
        } else {
            throw new PhemaNotImplementedException("The translator doesn't translate this unit");
        }
        return value;
    }

    private static Expression getExpressionOfType(List<Expression> list, Class type) {
        for (Expression expr : list) {
            if (expr.getClass().equals(type)) {
                return expr;
            }
        }

        return null;
    }

    /**
     * Helper method to take an ExpressionRef expression and convert it into an OHDSI InclusionRule.  This is used when the top-level
     * expression for a phenotype is a reference to another expression.
     *
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static InclusionRule generateInclusionRuleForExpressionRef(ExpressionRef expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        InclusionRule inclusionRule = new InclusionRule(expression.getName());
        CriteriaListEntry entry = generateCriteriaListEntryForExpression(expression, library, conceptSets);
        CriteriaList criteriaList = new CriteriaList() {{
            addEntry(entry);
        }};
        InclusionExpression inclusionExpression = new InclusionExpression(InclusionExpression.Type.All, criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        return inclusionRule;
    }

    /**
     * Helper method to take a BinaryExpression expression and convert it into an OHDSI InclusionRule.  This is used when the top-level
     * expression for a phenotype is a boolean rule (e.g., And, Or) - which derive from BinaryExpression.
     *
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static List<InclusionRule> generateInclusionRulesForBinaryExpression(BinaryExpression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        List<InclusionRule> inclusionRules = new ArrayList<InclusionRule>();

        InclusionRule inclusionRule = new InclusionRule(getBooleanExpressionName(expression));
        CriteriaList criteriaList = new CriteriaList();
        InclusionExpression inclusionExpression = new InclusionExpression(getInclusionExpressionType(expression), criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        List<Expression> operands = expression.getOperand();
        for (Expression operandExp : operands) {
            // If we have an expression reference, we really need to look ahead and figure out what it is.  That way
            // we can properly translate the target expression.
            if (operandExp instanceof ExpressionRef) {
                operandExp = getExpressionReferenceTarget((ExpressionRef) operandExp, library);
            }

            if (isBooleanExpression(operandExp)) {
                inclusionExpression.addInclusionGroups(
                    generateInclusionRulesForBinaryExpression((BinaryExpression) operandExp, library, conceptSets)
                        .stream()
                        .map(x -> x.getExpression())
                        .collect(Collectors.toList()));
                continue;
            }
            CriteriaListEntry entry = generateCriteriaListEntryForExpression(operandExp, library, conceptSets);
            if (entry == null) {
                throw new PhemaNotImplementedException("The translator was unable to process this type of expression");
            }

            criteriaList.addEntry(entry);
        }

        inclusionRules.add(inclusionRule);

        return inclusionRules;
    }

    private static CriteriaGroup getCriteriaGroupForBinaryExpression(BinaryExpression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        CriteriaGroup criteriaGroup = new CriteriaGroup();
        criteriaGroup.type = getInclusionExpressionType(expression);

        List<Expression> operands = expression.getOperand();
        for (Expression operandExp : operands) {
            // If we have an expression reference, we really need to look ahead and figure out what it is.  That way
            // we can properly translate the target expression.
            if (operandExp instanceof ExpressionRef) {
                operandExp = getExpressionReferenceTarget((ExpressionRef) operandExp, library);
            }

            if (isBooleanExpression(operandExp)) {
                criteriaGroup.groups = CirceUtil.addCriteriaGroup(criteriaGroup.groups,
                    getCriteriaGroupForBinaryExpression((BinaryExpression) operandExp, library, conceptSets));
                continue;
            }
            CorelatedCriteria corelatedCriteria = generateCorelatedCriteriaForExpression(operandExp, library, conceptSets);
            if (corelatedCriteria == null) {
                throw new PhemaNotImplementedException("The translator was unable to process this type of expression");
            }

            criteriaGroup.criteriaList = CirceUtil.addCorelatedCriteria(criteriaGroup.criteriaList, corelatedCriteria);
        }

        return criteriaGroup;
    }

    /**
     * Helper method to generate a Criteria given a ConceptSet
     *
     * @param conceptSet
     * @return
     */
    private static Criteria generateCriteria(ConceptSet conceptSet, InclusionRule correlatedCriteriaRule) {
        // TODO - Can't assume it's an occurrence.  Need to map between QDM/FHIR and OHDSI types
        ConditionOccurrence conditionOccurrence = new ConditionOccurrence(Integer.toString(conceptSet.getId()));
        if (correlatedCriteriaRule != null) {
            conditionOccurrence.setCorrelatedCriteria(correlatedCriteriaRule.getExpression());
        }
        Criteria criteria = new Criteria(conditionOccurrence);
        return criteria;
    }

    private static org.ohdsi.circe.cohortdefinition.Criteria generateCriteria2(ConceptSet conceptSet, CriteriaGroup corelatedCriteria) {
        // TODO - Can't assume it's an occurrence.  Need to map between QDM/FHIR and OHDSI types
        org.ohdsi.circe.cohortdefinition.ConditionOccurrence conditionOccurrence = new org.ohdsi.circe.cohortdefinition.ConditionOccurrence();
        conditionOccurrence.codesetId = conceptSet.getId();

        if (corelatedCriteria != null) {
            conditionOccurrence.CorrelatedCriteria = corelatedCriteria;
        }

        return conditionOccurrence;
    }

    /**
     * Given an ExpressionRef from CQL/ELM, convert it into an OHDSI CriteriaListEntry.
     *
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static CriteriaListEntry generateCriteriaListEntryForExpression(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Expression referencedExp = expression;
        if (expression instanceof ExpressionRef) {
            referencedExp = getExpressionReferenceTarget((ExpressionRef) expression, library);
        }

        Retrieve retrieveExpression = null;
        Occurrence occurrence = new Occurrence(Occurrence.Type.AtLeast, 1);
        if (referencedExp instanceof Retrieve) {
            retrieveExpression = (Retrieve) referencedExp;
        } else if (referencedExp instanceof Exists) {
            return generateCriteriaListEntryForExpression(((Exists) referencedExp).getOperand(), library, conceptSets);
        } else if (referencedExp instanceof Query) {
            retrieveExpression = getQueryRetrieveExpression((Query) referencedExp);
        } else if (isNumericComparison(referencedExp)) {
            occurrence = getNumericComparisonOccurrence((BinaryExpression) referencedExp);
            retrieveExpression = getBinaryExpressionRetrieveExpression((BinaryExpression) referencedExp);
        } else {
            // TODO - Need to handle more than simple query types
            throw new PhemaNotImplementedException(String.format("Currently the translator is only able to process Query and Retrieve expressions"));
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        CriteriaListEntry entry = new CriteriaListEntry();
        entry.setOccurrence(occurrence);
        entry.setCriteria(generateCriteria(matchedSet, null));
        return entry;
    }


    private static CorelatedCriteria generateCorelatedCriteriaForExpression(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Expression referencedExp = expression;
        if (expression instanceof ExpressionRef) {
            referencedExp = getExpressionReferenceTarget((ExpressionRef) expression, library);
        }

        Retrieve retrieveExpression = null;
        org.ohdsi.circe.cohortdefinition.Occurrence occurrence = CirceUtil.defaultOccurrence();

        if (referencedExp instanceof Retrieve) {
            retrieveExpression = (Retrieve) referencedExp;
        } else if (referencedExp instanceof Exists) {
            return generateCorelatedCriteriaForExpression(((Exists) referencedExp).getOperand(), library, conceptSets);
        } else if (referencedExp instanceof Query) {
            retrieveExpression = getQueryRetrieveExpression((Query) referencedExp);
        } else if (isNumericComparison(referencedExp)) {
            occurrence = getNumericComparisonOccurrence2((BinaryExpression) referencedExp);
            retrieveExpression = getBinaryExpressionRetrieveExpression((BinaryExpression) referencedExp);
        } else {
            // TODO - Need to handle more than simple query types
            throw new PhemaNotImplementedException(String.format("Currently the translator is only able to process Query and Retrieve expressions"));
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        CorelatedCriteria corelatedCriteria = CirceUtil.defaultCorelatedCriteria();
        corelatedCriteria.occurrence = occurrence;
        corelatedCriteria.criteria = generateCriteria2(matchedSet, null);

        return corelatedCriteria;
    }


    private static CriteriaGroup generateCriteriaGroupForExpression(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Expression referencedExp = expression;
        if (expression instanceof ExpressionRef) {
            referencedExp = getExpressionReferenceTarget((ExpressionRef) expression, library);
        }

        Retrieve retrieveExpression = null;
        org.ohdsi.circe.cohortdefinition.Occurrence occurrence = CirceUtil.defaultOccurrence();

        if (referencedExp instanceof Retrieve) {
            retrieveExpression = (Retrieve) referencedExp;
        } else if (referencedExp instanceof Exists) {
            return generateCriteriaGroupForExpression(((Exists) referencedExp).getOperand(), library, conceptSets);
        } else if (referencedExp instanceof Query) {
            retrieveExpression = getQueryRetrieveExpression((Query) referencedExp);
        } else if (isNumericComparison(referencedExp)) {
            occurrence = getNumericComparisonOccurrence2((BinaryExpression) referencedExp);
            retrieveExpression = getBinaryExpressionRetrieveExpression((BinaryExpression) referencedExp);
        } else {
            // TODO - Need to handle more than simple query types
            throw new PhemaNotImplementedException(String.format("Currently the translator is only able to process Query and Retrieve expressions"));
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        CorelatedCriteria corelatedCriteria = CirceUtil.defaultCorelatedCriteria();
        corelatedCriteria.occurrence = occurrence;
        corelatedCriteria.criteria = generateCriteria2(matchedSet, null);

        CriteriaGroup criteriaGroup = new CriteriaGroup();
        criteriaGroup.criteriaList = new CorelatedCriteria[]{corelatedCriteria};

        return criteriaGroup;
    }

    /**
     * Helper method to take an expression reference, and track back to the object that it refers to.
     *
     * @param expressionRef
     * @param library
     * @return
     * @throws Exception
     */
    private static Expression getExpressionReferenceTarget(ExpressionRef expressionRef, Library library) throws Exception {
        Optional<ExpressionDef> referencedExpDef = library.getStatements().getDef().stream().filter(x -> x.getName().equals(expressionRef.getName())).findFirst();
        if (!referencedExpDef.isPresent()) {
            // TODO - This could be because things are referenced in other libraries.  Will need to handle that situation.
            throw new Exception(String.format("Could not find the referenced expression %s in the library", expressionRef.getName()));
        }

        return referencedExpDef.get().getExpression();
    }

    /**
     * Helper method to extract a Retrieve expression from within a BinaryExpression.  This is assuming that the
     * BinaryExpression is of a type that contains a Count (e.g., Greater, Less).
     *
     * @param referencedExp
     * @return
     * @throws Exception
     */
    private static Retrieve getBinaryExpressionRetrieveExpression(BinaryExpression referencedExp) throws Exception {
        List<Expression> operands = referencedExp.getOperand();
        Optional<Expression> countOperand = operands.stream().filter(x -> (x instanceof Count)).findFirst();
        if (!countOperand.isPresent()) {
            throw new Exception("The translator expected to find a Count expression, but one was not present");
        }

        Expression sourceExpression = ((Count) countOperand.get()).getSource();
        if (!(sourceExpression instanceof Retrieve)) {
            throw new Exception("The translator expected to find a Retrieve expression as the operand of a Count");
        }
        return (Retrieve) sourceExpression;
    }

    /**
     * Helper method to extract the Occurrence information from a BinaryExpression.  This is assuming that the
     * BinaryExpression is of a type that contains a Count (e.g., Greater, Less).
     *
     * @param referencedExp
     * @return
     * @throws Exception
     */
    private static Occurrence getNumericComparisonOccurrence(BinaryExpression referencedExp) throws Exception {
        List<Expression> operands = referencedExp.getOperand();
        // We are assuming there are 2 operands to build an occurrence.  If that's violated, we throw an exception.  At
        // that point we'll need to revisit what to do to expand our assumptions.
        boolean hasCount = false;
        String countString = null;

        for (Expression operand : operands) {
            if (operand instanceof Count) {
                hasCount = true;
            } else if (operand instanceof Literal) {
                countString = ((Literal) operand).getValue();
            }
        }
        if (!hasCount || countString == null) {
            throw new Exception("The translator expected an expression with a Count and Literal operand, but these were not found.");
        }

        int countValue = Integer.parseInt(countString);
        Occurrence occurrence = new Occurrence(Occurrence.Type.AtLeast, countString);
        if (referencedExp instanceof Greater) {
            occurrence.setType(Occurrence.Type.AtLeast);
            // Because OHDSI uses "at least" (which is >=), we adjust the count value for equivalency
            occurrence.setCount(countValue + 1);
        } else if (referencedExp instanceof GreaterOrEqual) {
            occurrence.setType(Occurrence.Type.AtLeast);
            occurrence.setCount(countString);
        } else if (referencedExp instanceof Equal) {
            occurrence.setType(Occurrence.Type.Exactly);
        } else if (referencedExp instanceof Less) {
            occurrence.setType(Occurrence.Type.AtMost);
            // Because OHDSI uses "at most" (which is <=), we adjust the count value for equivalency
            occurrence.setCount(countValue - 1);
        } else if (referencedExp instanceof LessOrEqual) {
            occurrence.setType(Occurrence.Type.AtMost);
        }
        return occurrence;
    }

    private static org.ohdsi.circe.cohortdefinition.Occurrence getNumericComparisonOccurrence2(BinaryExpression referencedExp) throws Exception {
        List<Expression> operands = referencedExp.getOperand();
        // We are assuming there are 2 operands to build an occurrence.  If that's violated, we throw an exception.  At
        // that point we'll need to revisit what to do to expand our assumptions.
        boolean hasCount = false;
        String countString = null;

        for (Expression operand : operands) {
            if (operand instanceof Count) {
                hasCount = true;
            } else if (operand instanceof Literal) {
                countString = ((Literal) operand).getValue();
            }
        }
        if (!hasCount || countString == null) {
            throw new Exception("The translator expected an expression with a Count and Literal operand, but these were not found.");
        }

        int countValue = Integer.parseInt(countString);

        org.ohdsi.circe.cohortdefinition.Occurrence occurrence = CirceUtil.defaultOccurrence();
        occurrence.count = countValue;

        if (referencedExp instanceof Greater) {
            occurrence.type = org.ohdsi.circe.cohortdefinition.Occurrence.AT_LEAST;
            // Because OHDSI uses "at least" (which is >=), we adjust the count value for equivalency
            occurrence.count++;
        } else if (referencedExp instanceof GreaterOrEqual) {
            occurrence.type = org.ohdsi.circe.cohortdefinition.Occurrence.AT_LEAST;
        } else if (referencedExp instanceof Equal) {
            occurrence.type = org.ohdsi.circe.cohortdefinition.Occurrence.EXACTLY;
        } else if (referencedExp instanceof Less) {
            occurrence.type = org.ohdsi.circe.cohortdefinition.Occurrence.AT_MOST;
            // Because OHDSI uses "at most" (which is <=), we adjust the count value for equivalency
            occurrence.count--;
        } else if (referencedExp instanceof LessOrEqual) {
            occurrence.type = org.ohdsi.circe.cohortdefinition.Occurrence.AT_MOST;
        }
        return occurrence;
    }

    /**
     * Given a Retrieve ELM expression, determine the resulting value set (OHDSI ConceptSet) that we need to retrieve
     *
     * @param retrieveExpression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static ConceptSet getConceptSetForRetrieve(Retrieve retrieveExpression, Library library, List<ConceptSet> conceptSets) throws Exception {
        // TODO  would be nice to have a convenience method to enumerate out all the codes of interest.
        if (!(retrieveExpression.getCodes() instanceof ValueSetRef)) {
            throw new PhemaNotImplementedException("Currently the translator is only able to handle ValueSetRef query sources");
        }
        // TODO - we search by name, but should really be searching by Library and Name (will need more than just the Library passed in
        ValueSetRef valueSet = (ValueSetRef) retrieveExpression.getCodes();
        Optional<ValueSetDef> valueSetDef = library.getValueSets().getDef().stream().filter(x -> x.getName().equals(valueSet.getName())).findFirst();
        if (!valueSetDef.isPresent()) {
            // TODO - This could be because things are referenced in other libraries.  Will need to handle that situation.
            throw new Exception(String.format("Could not find the referenced value set %s in the library", valueSet.getName()));
        }

        ConceptSet matchedSet = findConceptSetByOid(conceptSets, valueSetDef.get().getId());
        if (matchedSet == null) {
            throw new Exception(String.format("Failed to find the value set referenced with OID %s", valueSetDef.get().getId()));
        }

        return matchedSet;
    }

    private static org.ohdsi.circe.cohortdefinition.ConceptSet getConceptSetForRetrieve2(Retrieve retrieveExpression, Library library, List<ConceptSet> conceptSets) throws Exception {
        // TODO  would be nice to have a convenience method to enumerate out all the codes of interest.
        if (!(retrieveExpression.getCodes() instanceof ValueSetRef)) {
            throw new PhemaNotImplementedException("Currently the translator is only able to handle ValueSetRef query sources");
        }
        // TODO - we search by name, but should really be searching by Library and Name (will need more than just the Library passed in
        ValueSetRef valueSet = (ValueSetRef) retrieveExpression.getCodes();
        Optional<ValueSetDef> valueSetDef = library.getValueSets().getDef().stream().filter(x -> x.getName().equals(valueSet.getName())).findFirst();
        if (!valueSetDef.isPresent()) {
            // TODO - This could be because things are referenced in other libraries.  Will need to handle that situation.
            throw new Exception(String.format("Could not find the referenced value set %s in the library", valueSet.getName()));
        }

        org.ohdsi.circe.cohortdefinition.ConceptSet matchedSet = CirceUtil.convertConceptSetToCirce(findConceptSetByOid(conceptSets, valueSetDef.get().getId()));
        if (matchedSet == null) {
            throw new Exception(String.format("Failed to find the value set referenced with OID %s", valueSetDef.get().getId()));
        }

        return matchedSet;
    }

    /**
     * Query expressions ultimately have a Retrieve expression embedded in them.  This helper method gets us to
     * the Retrieve expression, with a few checks along the way.
     *
     * @param query
     * @return
     * @throws Exception
     */
    private static Retrieve getQueryRetrieveExpression(Query query) throws PhemaNotImplementedException {
        int querySourceSize = query.getSource().size();
        if (querySourceSize != 1) {
            // TODO - will of course need to be more flexible here
            throw new PhemaNotImplementedException(String.format("Currently the translator is only able to handle a single Query source"));
        }

        Expression aliasExpression = query.getSource().get(0).getExpression();
        if (!(aliasExpression instanceof Retrieve)) {
            throw new PhemaNotImplementedException("Currently the translator is only able to handle Retrieve query sources");
        }

        return (Retrieve) aliasExpression;
    }

    /**
     * Exists expressions ultimately have a Retrieve expression embedded in them.  This helper method gets us to
     * the Retrieve expression, with a few checks along the way.
     *
     * @param expression
     * @return
     * @throws Exception
     */
    private static Retrieve getExistsRetrieveExpression(Exists expression) throws PhemaNotImplementedException {
        Expression aliasExpression = expression.getOperand();
        if (!(aliasExpression instanceof Retrieve)) {
            throw new PhemaNotImplementedException("Currently the translator is only able to handle Retrieve query sources");
        }

        return (Retrieve) aliasExpression;
    }

    /**
     * Given a value set OID, find the corresponding ConceptSet from our list of loaded concept sets
     *
     * @param conceptSets
     * @param oid
     * @return
     */
    private static ConceptSet findConceptSetByOid(List<ConceptSet> conceptSets, String oid) {
        Optional<ConceptSet> conceptSet = conceptSets.stream().filter(x -> x.getOid().equals(oid) ||
            x.getOid().endsWith(oid)) // endsWith allows us to ignore namespace prefixes
            .findFirst();
        return conceptSet.isPresent() ? conceptSet.get() : null;
    }

    /**
     * Determine the inclusion expression type (a string constant) to use, given an expression.
     *
     * @param expression
     * @return
     * @throws Exception
     */
    private static String getInclusionExpressionType(Expression expression) throws PhemaNotImplementedException {
        if (expression instanceof Or) {
            return InclusionExpression.Type.Any;
        } else if (expression instanceof And) {
            return InclusionExpression.Type.All;
        }

        throw new PhemaNotImplementedException("Currently the translator only handles And and Or expressions");
    }

    private static CirceConstants.CriteriaGroupType getInclusionExpressionType2(Expression expression) throws PhemaNotImplementedException {
        if (expression instanceof Or) {
            return CirceConstants.CriteriaGroupType.ANY;
        } else if (expression instanceof And) {
            return CirceConstants.CriteriaGroupType.ALL;
        }

        throw new PhemaNotImplementedException("Currently the translator only handles And and Or expressions");
    }

    /**
     * Helper method to generate a name for a boolean (e.g., And, Or) expression
     *
     * @param expression
     * @return
     */
    private static String getBooleanExpressionName(Expression expression) {
        if (expression instanceof Or) {
            return "One or more of the following";
        } else if (expression instanceof And) {
            return "All of the following";
        }

        return expression.getClass().toString();
    }
}
