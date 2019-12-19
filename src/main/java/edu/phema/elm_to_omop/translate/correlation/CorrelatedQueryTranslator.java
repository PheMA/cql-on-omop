package edu.phema.elm_to_omop.translate.correlation;

import edu.phema.elm_to_omop.translate.PhemaElmaToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.PhemaTranslationException;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorrelatedQueryTranslator {

    private static List<String> getCorrelatedEntities(Query query) throws PhemaNotImplementedException {
        List<String> entities = new ArrayList<>();

        if (query.getSource().size() > 1) {
            throw new PhemaNotImplementedException("Only single source query expressions are currently supported");
        }

        entities.add(((Retrieve) query.getSource().get(0).getExpression()).getDataType().getLocalPart());
        entities.add(((Retrieve) query.getRelationship().get(0).getExpression()).getDataType().getLocalPart());

        return entities;
    }

    private static boolean correlationExpressionSupported(Expression correlationExpression) {
        // For now we support only simple equal correlation like Encounter.id = Condition.encounter
        // TODO: Add temporal operators here
        if (correlationExpression instanceof Equal) {
            return true;
        } else {
            return false;
        }
    }

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

    private static QuickResourceAttributePair getResourceAttributePairFromOperand(Expression operand, Map<String, QuickResource> aliases) throws CorrelationException {
        if (operand instanceof Property) {
            return new QuickResourceAttributePair(aliases.get(((Property) operand).getScope()), CorrelationConstants.QuickResourceAttribute.create(((Property) operand).getPath()));
        } else if (operand instanceof As) {
            return getResourceAttributePairFromOperand(((As) operand).getOperand(), aliases);
        } else {
            throw new CorrelationException("Correlation not supported for operand type: " + operand.getClass().getSimpleName());
        }
    }

    private static Correlation getCorrelation(Query query) throws PhemaNotImplementedException, CorrelationException {
        List<String> attributes = new ArrayList<>();

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

    public static CriteriaGroup generatedCriteriaGroupForCorrelatedQuery(Query query, PhemaElmaToOmopTranslatorContext context) throws
        PhemaNotImplementedException, CorrelationException, PhemaTranslationException {

        Correlation correlation = getCorrelation(query);

        CorrelatedQueryCriteriaGroupGeneratorFunction<Correlation, PhemaElmaToOmopTranslatorContext, CriteriaGroup> generator = CorrelationConstants.generators.get(correlation);

        if (generator == null) {
            throw new CorrelationException(String.format("Unsupported correlation: %s", correlation.toString()));
        } else {
            return generator.apply(correlation, context);
        }
    }
}
