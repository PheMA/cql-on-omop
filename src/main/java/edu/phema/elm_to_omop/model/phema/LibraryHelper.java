package edu.phema.elm_to_omop.model.phema;

import edu.phema.elm_to_omop.model.omop.*;
import org.hl7.elm.r1.*;
import org.hl7.elm.r1.Expression;

import java.util.List;
import java.util.Optional;

/**
 * Extension of the CQL Library class to provide utility functions
 */
public class LibraryHelper {
    public static InclusionRule generateInclusionRule(Library library, Expression expression, java.util.List<ConceptSet> conceptSets) throws Exception {
        // Track our bad assumptions, just so we know we're coming back to fix them
        // TODO We are assuming this is a binary type.  It could be just a single, simple expression by itself.
        if (!(expression instanceof BinaryExpression)) {
            throw new Exception("Currently the translator is only able to process boolean expressions");
        }

        BinaryExpression binaryExpression = (BinaryExpression)expression;
        InclusionRule inclusionRule = new InclusionRule(getExpressionName(binaryExpression));
        List<Expression> operands = binaryExpression.getOperand();
        CriteriaList criteriaList = new CriteriaList();
        for (Expression operandExp : operands) {
            // TODO - It could be lots of things - we're starting with simple existence checks
            if (!(operandExp instanceof Exists)) {
                throw new Exception("Currently the translator is only able to process Exists expressions");
            }

            Exists existExp = (Exists)operandExp;
            Expression existOperandExp = existExp.getOperand();
            // TODO - We need to figure out what we can process besides expression references
            if (!(existOperandExp instanceof ExpressionRef)) {
                throw new Exception("Currently the translator is only able to process ExpressionRef entries");
            }

            ExpressionRef ref = (ExpressionRef)existOperandExp;
            Optional<ExpressionDef> referencedExpDef = library.getStatements().getDef().stream().filter(x -> x.getName().equals(ref.getName())).findFirst();
            if (!referencedExpDef.isPresent()) {
                // TODO - This could be because things are referenced in other libraries.  Will need to handle that situation.
                throw new Exception(String.format("Could not find the referenced expression %s in the library", ref.getName()));
            }

            Expression referencedExp = referencedExpDef.get().getExpression();
            if (!(referencedExp instanceof Query)) {
                // TODO - Need to handle more than simple query types
                throw new Exception(String.format("Currently the translator is only able to process Query expressions"));
            }
            Query query = (Query)referencedExp;
            int querySourceSize = query.getSource().size();
            if (querySourceSize != 1) {
                // TODO - will of course need to be more flexible here
                throw new Exception(String.format("Currently the translator is only able to handle a single Query source"));
            }

            Expression aliasExpression = query.getSource().get(0).getExpression();
            if (!(aliasExpression instanceof Retrieve)) {
                throw new Exception("Currently the translator is only able to handle Retrieve query sources");
            }

            // TODO  would be nice ot have a convenience method to enumerate out all the codes of interest.
            Retrieve retrieveExpression = (Retrieve)aliasExpression;
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

            // TODO - Can't assume it's an occurrence.  Need to map between QDM/FHIR/OHDSI types
            ConditionOccurrence conditionOccurrence = new ConditionOccurrence(Integer.toString(matchedSet.getId()));
            Criteria criteria = new Criteria(conditionOccurrence);
            CriteriaListEntry entry = new CriteriaListEntry();
            entry.setCriteria(criteria);
        }

        return inclusionRule;
    }

    private static ConceptSet findConceptSetByOid(List<ConceptSet> conceptSets, String oid) {
        Optional<ConceptSet> conceptSet = conceptSets.stream().filter(x -> x.getOid().equals(oid) ||
                x.getOid().endsWith(oid)) // endsWith allows us to ignore namespace prefixes
            .findFirst();
        return conceptSet.isPresent() ? conceptSet.get() : null;
    }

    private static String getExpressionName(Expression expression) {
        if (expression instanceof Or) {
            return "One or more of the following";
        }
        else if (expression instanceof And) {
            return "All of the following";
        }

        return expression.getClass().toString();
    }
}
