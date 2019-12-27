package edu.phema.elm_to_omop.translate.criteria.correlation;

import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for generated Circe CorelatedCriteria for CQL correlated Query expressions. Right now only support
 * correlation between two sounds with a single relationship. We also only support Equal as the correlation expression.
 */
public class CorrelatedQueryTranslator {

    /**
     * Determin if we support the given Expression as a correlation expression
     *
     * @param correlationExpression The correlation expression
     * @return True if we support it, false otherwise
     */
    private static boolean correlationExpressionSupported(Expression correlationExpression) {
        // For now we support only simple equal correlation like Encounter.id = Condition.encounter
        // TODO: Add temporal operators here
        if (correlationExpression instanceof Equal) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets up a map from aliased query sources to QuickResource instances. This pre-processing steps makes it easier
     * to build CorelatedCriteria later
     *
     * @param query The ELM Query expression containing the AliasedQuerySource expressions
     * @return A map from aliases to QuickResource instances
     * @throws CorrelationException
     */
    private static Map<String, QuickResource> setupAliases(Query query) throws CorrelationException {
        Map<String, QuickResource> aliases = new HashMap<>();

        AliasedQuerySource outerAliasedExpression = query.getSource().get(0);
        Retrieve outerRetrieve = (Retrieve) outerAliasedExpression.getExpression();
        String outerResourceType = outerRetrieve.getDataType().getLocalPart();
        String outerValuesetFilter = ((ValueSetRef) (outerRetrieve).getCodes()).getName();

        RelationshipClause innerAliasedExpression = query.getRelationship().get(0);
        Retrieve innerRetrieve = (Retrieve) innerAliasedExpression.getExpression();
        String innerResourceType = innerRetrieve.getDataType().getLocalPart();
        String innerValuesetFilter = ((ValueSetRef) (innerRetrieve).getCodes()).getName();

        aliases.put(outerAliasedExpression.getAlias(), QuickResource.from(outerResourceType, outerValuesetFilter));
        aliases.put(innerAliasedExpression.getAlias(), QuickResource.from(innerResourceType, innerValuesetFilter));

        return aliases;
    }

    /**
     * Builds a QuickResourceAttributePair from an Expression. We use these objects to track which resources and
     * attributes are involved in the correlation
     *
     * @param operand The correlated Query relationship SuchThat expression
     * @param aliases A map from aliases to QuickResources
     * @return The QuickResourceAttributePair representing the entity and attribute involved in the correlation
     * @throws CorrelationException
     */
    private static QuickResourceAttributePair getResourceAttributePairFromOperand(Expression operand, Map<String, QuickResource> aliases) throws CorrelationException {
        if (operand instanceof Property) {
            return new QuickResourceAttributePair(aliases.get(((Property) operand).getScope()), CorrelationConstants.QuickResourceAttribute.create(((Property) operand).getPath()));
        } else if (operand instanceof As) {
            return getResourceAttributePairFromOperand(((As) operand).getOperand(), aliases);
        } else {
            throw new CorrelationException("Correlation not supported for operand type: " + operand.getClass().getSimpleName());
        }
    }

    /**
     * Builds up Correlation object, which consts of two QuickResourceAttributePair and an ELM correlation expression
     *
     * @param query The query from which to build the Correlation
     * @return The created Correlation object
     * @throws PhemaNotImplementedException
     * @throws CorrelationException
     */
    private static Correlation getCorrelation(Query query) throws PhemaNotImplementedException, CorrelationException {
        Expression correlationExpression = query.getRelationship().get(0).getSuchThat();

        if (!correlationExpressionSupported(correlationExpression)) {
            throw new PhemaNotImplementedException(String.format("Correlation not support for %s expression", correlationExpression.getClass().getSimpleName()));
        }

        Map<String, QuickResource> aliases = setupAliases(query);

        if (correlationExpression instanceof Equal) {
            QuickResourceAttributePair lhs = getResourceAttributePairFromOperand(((Equal) correlationExpression).getOperand().get(0), aliases);
            QuickResourceAttributePair rhs = getResourceAttributePairFromOperand(((Equal) correlationExpression).getOperand().get(1), aliases);

            return new Correlation(lhs, rhs, correlationExpression);
        } else {
            throw new PhemaNotImplementedException(String.format("Correlation not support for %s expression", correlationExpression.getClass().getSimpleName()));
        }
    }

    /**
     * Creates a Circe CorelationCriteria from an ELM correlated Query expression
     *
     * @param query   The correlated Query expression
     * @param context The translation context
     * @return The created CorelationCriteria
     * @throws PhemaNotImplementedException
     * @throws CorrelationException
     * @throws PhemaTranslationException
     */
    public static CorelatedCriteria generateCorelatedCriteriaForCorrelatedQuery(Query query, PhemaElmToOmopTranslatorContext context) throws
        PhemaNotImplementedException, CorrelationException, PhemaTranslationException {

        Correlation correlation = getCorrelation(query);

        CorrelatedQueryCorelatedCriteriaGeneratorFunction<Correlation, PhemaElmToOmopTranslatorContext, CorelatedCriteria> generator = CorrelationConstants.generators.get(correlation);

        if (generator == null) {
            throw new CorrelationException(String.format("Unsupported correlation: %s", correlation.toString()));
        } else {
            return generator.apply(correlation, context);
        }
    }
}
