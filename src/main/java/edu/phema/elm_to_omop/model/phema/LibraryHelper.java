package edu.phema.elm_to_omop.model.phema;

import edu.phema.elm_to_omop.model.omop.*;
import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Expression;

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
     * @param library The CQL/ELM library that the expression is from
     * @param expression The expression to translate into a phenotype
     * @param conceptSets The list of value sets (concept sets) related to the CQL/ELM definition
     * @return An InclusionRule that can be included in the OHDSI phenotype definition
     * @throws Exception
     */
    public static List<InclusionRule> generateInclusionRules(Library library, Expression expression, java.util.List<ConceptSet> conceptSets) throws Exception {
        List<InclusionRule> inclusionRules = new ArrayList<InclusionRule>();
        if (isNumericComparison(expression)) {
            inclusionRules.add(generateInclusionRuleForNumericComparison(expression, library, conceptSets));
        }
        else if (expression instanceof BinaryExpression) {
            inclusionRules.addAll(generateInclusionRulesForBinaryExpression((BinaryExpression)expression, library, conceptSets));
        }
        else if (expression instanceof Query || expression instanceof Exists) {
            inclusionRules.addAll(generateInclusionRulesForQueryOrExists(expression, library, conceptSets));
        }
        else if (expression instanceof ExpressionRef) {
            inclusionRules.add(generateInclusionRuleForExpressionRef((ExpressionRef)expression, library, conceptSets));
        }
        else {
            throw new Exception("The translator is currently unable to generate OHDSI inclusion rules for this type of expression");
        }

        return inclusionRules;
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
        CriteriaList criteriaList = new CriteriaList() {{ addEntry(entry); }};
        InclusionExpression inclusionExpression = new InclusionExpression(InclusionExpression.Type.All, criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        return inclusionRule;
    }

    /**
     * Helper method to take a Query expression and convert it into an OHDSI InclusionRule.  This is used when the top-level
     * expression for a phenotype is a simple query.
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static List<InclusionRule> generateInclusionRulesForQueryOrExists(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Retrieve retrieveExpression = null;
        if (expression instanceof Query) {
            retrieveExpression = getQueryRetrieveExpression((Query)expression);
        }
        else if (expression instanceof Exists) {
            Exists exists = (Exists)expression;
            if (exists.getOperand() instanceof ExpressionRef) {
                return generateInclusionRules(library, exists.getOperand(), conceptSets);
            }
            retrieveExpression = getExistsRetrieveExpression(exists);
        }

        if (retrieveExpression == null) {
            throw new Exception("Unable to generate an inclusion rule for the Query or Exists expression");
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        InclusionRule inclusionRule = new InclusionRule(matchedSet.getName());
        CriteriaListEntry entry = new CriteriaListEntry();
        // TODO - hardcoding for now
        entry.setOccurrence(new Occurrence(Occurrence.Type.AtLeast, "1"));
        entry.setCriteria(generateCriteria(matchedSet));

        CriteriaList criteriaList = new CriteriaList() {{ addEntry(entry); }};
        InclusionExpression inclusionExpression = new InclusionExpression(InclusionExpression.Type.All, criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        return new ArrayList<InclusionRule>() {{ add(inclusionRule); }};
    }

    /**
     * Helper method to take an ExpressionRef expression and convert it into an OHDSI InclusionRule.  This is used when the top-level
     * expression for a phenotype is a reference to another expression.
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static InclusionRule generateInclusionRuleForExpressionRef(ExpressionRef expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        InclusionRule inclusionRule = new InclusionRule(expression.getName());
        CriteriaListEntry entry = generateCriteriaListEntryForExpression(expression, library, conceptSets);
        CriteriaList criteriaList = new CriteriaList() {{ addEntry(entry); }};
        InclusionExpression inclusionExpression = new InclusionExpression(InclusionExpression.Type.All, criteriaList, null, null);
        inclusionRule.setExpression(inclusionExpression);
        return inclusionRule;
    }

    /**
     * Helper method to take a BinaryExpression expression and convert it into an OHDSI InclusionRule.  This is used when the top-level
     * expression for a phenotype is a boolean rule (e.g., And, Or) - which derive from BinaryExpression.
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
                operandExp = getExpressionReferenceTarget((ExpressionRef)operandExp, library);
            }

            if (isBooleanExpression(operandExp)) {
                inclusionExpression.addInclusionGroups(
                    generateInclusionRulesForBinaryExpression((BinaryExpression)operandExp, library, conceptSets)
                        .stream()
                        .map(x -> x.getExpression())
                        .collect(Collectors.toList()));
                continue;
            }
            CriteriaListEntry entry = generateCriteriaListEntryForExpression(operandExp, library, conceptSets);
            if (entry == null) {
                throw new Exception("The translator was unable to process this type of expression");
            }

            criteriaList.addEntry(entry);
        }

        inclusionRules.add(inclusionRule);

        return inclusionRules;
    }

    /**
     * Helper method to generate a Criteria given a ConceptSet
     * @param conceptSet
     * @return
     */
    private static Criteria generateCriteria(ConceptSet conceptSet) {
        // TODO - Can't assume it's an occurrence.  Need to map between QDM/FHIR and OHDSI types
        ConditionOccurrence conditionOccurrence = new ConditionOccurrence(Integer.toString(conceptSet.getId()));
        Criteria criteria = new Criteria(conditionOccurrence);
        return criteria;
    }

    /**
     * Given an ExpressionRef from CQL/ELM, convert it into an OHDSI CriteriaListEntry.
     * @param expression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static CriteriaListEntry generateCriteriaListEntryForExpression(Expression expression, Library library, List<ConceptSet> conceptSets) throws Exception {
        Expression referencedExp = expression;
        if (expression instanceof ExpressionRef) {
            referencedExp = getExpressionReferenceTarget((ExpressionRef)expression, library);
        }

        Retrieve retrieveExpression = null;
        Occurrence occurrence = new Occurrence(Occurrence.Type.AtLeast, 1);
        if (referencedExp instanceof Retrieve) {
            retrieveExpression = (Retrieve)referencedExp;
        }
        else if (referencedExp instanceof Exists) {
            return generateCriteriaListEntryForExpression(((Exists)referencedExp).getOperand(), library, conceptSets);
        }
        else if (referencedExp instanceof Query) {
            retrieveExpression = getQueryRetrieveExpression((Query)referencedExp);
        }
        else if (isNumericComparison(referencedExp)) {
            occurrence = getNumericComparisonOccurrence((BinaryExpression)referencedExp);
            retrieveExpression = getBinaryExpressionRetrieveExpression((BinaryExpression)referencedExp);
        }
        else {
            // TODO - Need to handle more than simple query types
            throw new Exception(String.format("Currently the translator is only able to process Query and Retrieve expressions"));
        }

        ConceptSet matchedSet = getConceptSetForRetrieve(retrieveExpression, library, conceptSets);

        CriteriaListEntry entry = new CriteriaListEntry();
        entry.setOccurrence(occurrence);
        entry.setCriteria(generateCriteria(matchedSet));
        return entry;
    }

    /**
     * Helper method to take an expression reference, and track back to the object that it refers to.
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

        Expression sourceExpression =  ((Count)countOperand.get()).getSource();
        if (!(sourceExpression instanceof Retrieve)) {
            throw new Exception("The translator expected to find a Retrieve expression as the operand of a Count");
        }
        return (Retrieve)sourceExpression;
    }

    /**
     * Helper method to extract the Occurrence information from a BinaryExpression.  This is assuming that the
     * BinaryExpression is of a type that contains a Count (e.g., Greater, Less).
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
            }
            else if (operand instanceof Literal) {
                countString = ((Literal)operand).getValue();
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
        }
        else if (referencedExp instanceof GreaterOrEqual) {
            occurrence.setType(Occurrence.Type.AtLeast);
            occurrence.setCount(countString);
        }
        else if (referencedExp instanceof Equal) {
            occurrence.setType(Occurrence.Type.Exactly);
        }
        else if (referencedExp instanceof Less) {
            occurrence.setType(Occurrence.Type.AtMost);
            // Because OHDSI uses "at most" (which is <=), we adjust the count value for equivalency
            occurrence.setCount(countValue - 1);
        }
        else if (referencedExp instanceof LessOrEqual) {
            occurrence.setType(Occurrence.Type.AtMost);
        }
        return occurrence;
    }

    /**
     * Given a Retrieve ELM expression, determine the resulting value set (OHDSI ConceptSet) that we need to retrieve
     * @param retrieveExpression
     * @param library
     * @param conceptSets
     * @return
     * @throws Exception
     */
    private static ConceptSet getConceptSetForRetrieve(Retrieve retrieveExpression, Library library, List<ConceptSet> conceptSets) throws Exception {
        // TODO  would be nice to have a convenience method to enumerate out all the codes of interest.
        if (!(retrieveExpression.getCodes() instanceof ValueSetRef)) {
            throw new Exception("Currently the translator is only able to handle ValueSetRef query sources");
        }
        // TODO - we search by name, but should really be searching by Library and Name (will need more than just the Library passed in
        ValueSetRef valueSet = (ValueSetRef)retrieveExpression.getCodes();
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

    /**
     * Query expressions ultimately have a Retrieve expression embedded in them.  This helper method gets us to
     * the Retrieve expression, with a few checks along the way.
     * @param query
     * @return
     * @throws Exception
     */
    private static Retrieve getQueryRetrieveExpression(Query query) throws Exception {
        int querySourceSize = query.getSource().size();
        if (querySourceSize != 1) {
            // TODO - will of course need to be more flexible here
            throw new Exception(String.format("Currently the translator is only able to handle a single Query source"));
        }

        Expression aliasExpression = query.getSource().get(0).getExpression();
        if (!(aliasExpression instanceof Retrieve)) {
            throw new Exception("Currently the translator is only able to handle Retrieve query sources");
        }

        return (Retrieve)aliasExpression;
    }

    /**
     * Exists expressions ultimately have a Retrieve expression embedded in them.  This helper method gets us to
     * the Retrieve expression, with a few checks along the way.
     * @param expression
     * @return
     * @throws Exception
     */
    private static Retrieve getExistsRetrieveExpression(Exists expression) throws Exception {
        Expression aliasExpression = expression.getOperand();
        if (!(aliasExpression instanceof Retrieve)) {
            throw new Exception("Currently the translator is only able to handle Retrieve query sources");
        }

        return (Retrieve)aliasExpression;
    }

    /**
     * Given a value set OID, find the corresponding ConceptSet from our list of loaded concept sets
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
     * @param expression
     * @return
     * @throws Exception
     */
    private static String getInclusionExpressionType(Expression expression) throws Exception {
        if (expression instanceof Or) {
            return InclusionExpression.Type.Any;
        }
        else if (expression instanceof And) {
            return InclusionExpression.Type.All;
        }

        throw new Exception("Currently the translator only handles And and Or expressions");
    }

    /**
     * Helper method to generate a name for a boolean (e.g., And, Or) expression
     * @param expression
     * @return
     */
    private static String getBooleanExpressionName(Expression expression) {
        if (expression instanceof Or) {
            return "One or more of the following";
        }
        else if (expression instanceof And) {
            return "All of the following";
        }

        return expression.getClass().toString();
    }
}
