package edu.phema.elm_to_omop.translate.criteria.correlation;

import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.CorelatedCriteriaTranslator;
import edu.phema.elm_to_omop.translate.CriteriaGroupTranslator;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslator;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;

import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible for generated Circe CorelatedCriteria for CQL correlated Query expressions.
 * Right now only support correlation between two sounds with a single relationship. We also only
 * support Equal as the correlation expression.
 */
public class CorrelatedQueryTranslator {

  private CorrelatedQueryTranslator() {

  }

  /**
   * Determin if we support the given Expression as a correlation expression
   *
   * @param correlationExpression The correlation expression
   * @return True if we support it, false otherwise
   */
  private static boolean correlationExpressionSupported(Expression correlationExpression) {
    // For now we support only simple equal correlation like Encounter.id = Condition.encounter
    // TODO: Add temporal operators here
    return correlationExpression instanceof Equal;
  }

  /**
   * Sets up a map from aliased query sources to QuickResource instances. This pre-processing steps
   * makes it easier to build CorelatedCriteria later
   *
   * @param query The ELM Query expression containing the AliasedQuerySource expressions
   * @return A map from aliases to QuickResource instances
   */
  private static Map<String, QuickResource> setupAliases(Query query,
                                                         PhemaElmToOmopTranslatorContext context)
    throws CorrelationException, PhemaTranslationException {
    Map<String, QuickResource> aliases = new HashMap<>();

    AliasedQuerySource outerAliasedExpression = query.getSource().get(0);
    Retrieve outerRetrieve = (Retrieve) outerAliasedExpression.getExpression();
    String outerResourceType = outerRetrieve.getDataType().getLocalPart();
    String outerValuesetFilter = context.getVocabularyReferenceForRetrieve(outerRetrieve);

    RelationshipClause innerAliasedExpression = query.getRelationship().get(0);
    Retrieve innerRetrieve = (Retrieve) innerAliasedExpression.getExpression();
    String innerResourceType = innerRetrieve.getDataType().getLocalPart();
    String innerValuesetFilter = context.getVocabularyReferenceForRetrieve(innerRetrieve);

    aliases.put(outerAliasedExpression.getAlias(),
      QuickResource.from(outerResourceType, outerValuesetFilter));
    aliases.put(innerAliasedExpression.getAlias(),
      QuickResource.from(innerResourceType, innerValuesetFilter));

    return aliases;
  }

  /**
   * Builds a QuickResourceAttributePair from an Expression. We use these objects to track which
   * resources and attributes are involved in the correlation
   *
   * @param operand The correlated Query relationship SuchThat expression
   * @param aliases A map from aliases to QuickResources
   * @return The QuickResourceAttributePair representing the entity and attribute involved in the
   * correlation
   */
  private static QuickResourceAttributePair getResourceAttributePairFromOperand(Expression operand,
                                                                                Map<String, QuickResource> aliases) throws CorrelationException {
    if (operand instanceof Property) {
      return new QuickResourceAttributePair(aliases.get(((Property) operand).getScope()),
        CorrelationConstants.QuickResourceAttribute.create(((Property) operand).getPath()));
    } else if (operand instanceof As) {
      return getResourceAttributePairFromOperand(((As) operand).getOperand(), aliases);
    } else {
      throw new CorrelationException(
        "Correlation not supported for operand type: " + operand.getClass().getSimpleName());
    }
  }

  /**
   * Builds up Correlation object, which consts of two QuickResourceAttributePair and an ELM
   * correlation expression
   *
   * @param query The query from which to build the Correlation
   * @return The created Correlation object
   */
  private static Correlation getCorrelation(Query query, PhemaElmToOmopTranslatorContext context)
    throws PhemaNotImplementedException, CorrelationException, PhemaTranslationException {
    Expression correlationExpression = query.getRelationship().get(0).getSuchThat();

    if (!correlationExpressionSupported(correlationExpression)) {
      throw new PhemaNotImplementedException(String
        .format("Correlation not support for %s expression",
          correlationExpression.getClass().getSimpleName()));
    }

    Map<String, QuickResource> aliases = setupAliases(query, context);

    if (correlationExpression instanceof Equal) {
      QuickResourceAttributePair lhs = getResourceAttributePairFromOperand(
        ((Equal) correlationExpression).getOperand().get(0), aliases);
      QuickResourceAttributePair rhs = getResourceAttributePairFromOperand(
        ((Equal) correlationExpression).getOperand().get(1), aliases);

      return new Correlation(lhs, rhs, correlationExpression);
    } else {
      throw new PhemaNotImplementedException(String
        .format("Correlation not support for %s expression",
          correlationExpression.getClass().getSimpleName()));
    }
  }

  /**
   * Creates a Circe CorelationCriteria from an ELM correlated Query expression
   *
   * @param query   The correlated Query expression
   * @param context The translation context
   * @return The created CorelationCriteria
   */
  public static CorelatedCriteria generateCorelatedCriteriaForCorrelatedQuery(Query query,
                                                                              PhemaElmToOmopTranslatorContext context) throws
    PhemaNotImplementedException, CorrelationException, PhemaTranslationException {

    Correlation correlation = getCorrelation(query, context);

    CorrelatedQueryCorelatedCriteriaGeneratorFunction<Correlation, PhemaElmToOmopTranslatorContext, CorelatedCriteria> generator = CorrelationConstants.generators
      .get(correlation);

    if (generator == null) {
      throw new CorrelationException(
        String.format("Unsupported correlation: %s", correlation.toString()));
    } else {
      return generator.apply(correlation, context);
    }
  }

  public static boolean correlationWhereExpressionSupported(Expression expression) {
    return expression instanceof Exists || PhemaElmToOmopTranslator.isBooleanExpression(expression) || expression instanceof In;
  }

  public static CorelatedCriteria generateCorelatedCriteriaForCorrelatedQueryWithWhere(Query query, PhemaElmToOmopTranslatorContext context) throws
    Exception {
    Expression whereExpression = query.getWhere();

    if (!correlationWhereExpressionSupported(whereExpression)) {
      throw new CorrelationException(String.format("Unsupported correlation: %s", whereExpression.getClass().getSimpleName()));
    }

    // Push the alias to the context so we can access it in the nested expression
    String alias = context.addAlias(query);

    CorelatedCriteria outerCorrelateCriteria = CirceUtil.defaultCorelatedCriteria();

    if (whereExpression instanceof Exists || PhemaElmToOmopTranslator.isBooleanExpression(whereExpression)) {
      // descend into the nested expression
      CriteriaGroup criteriaGroup = CriteriaGroupTranslator.generateCriteriaGroupForExpression(whereExpression, context);

      outerCorrelateCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression((Retrieve) query.getSource().get(0).getExpression(), context);

      outerCorrelateCriteria.criteria.CorrelatedCriteria = criteriaGroup;

    } else if (whereExpression instanceof In) {
      // we are in the leaf expression (right now we only support "in Interval")

      // generate the criteria as if it was a retrieve
      outerCorrelateCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression((Retrieve) query.getSource().get(0).getExpression(), context);

      // then set up the occurrence dates based on the interval
      // TODO: Actually set up startWindow based on Interval - https://github.com/PheMA/cql-on-omop/issues/47
      //       We can probably make a lot of simplifying assumptions here, for example that all dates/durations are relative
      //       to the index date.
    }


    // Pop the alias to invalidate it
    context.removeAlias(alias);

    return outerCorrelateCriteria;
  }
}
