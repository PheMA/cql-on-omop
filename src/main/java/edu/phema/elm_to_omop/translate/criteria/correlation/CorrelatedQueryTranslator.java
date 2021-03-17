package edu.phema.elm_to_omop.translate.criteria.correlation;

import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.CorelatedCriteriaTranslator;
import edu.phema.elm_to_omop.translate.CriteriaGroupTranslator;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslator;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.criteria.comparison.ComparisonExpressionTranslator;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import edu.phema.elm_to_omop.translate.util.TemporalUtil;
import org.hl7.cql.model.DataType;
import org.hl7.cql.model.ListType;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.Measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  public static boolean correlationWhereExpressionSupported(Expression expression, String returnType) {
    return expression instanceof Exists ||
      PhemaElmToOmopTranslator.isBooleanExpression(expression) ||
      expression instanceof In ||
      // Support numeric comparisons for Observations/MEASUREMENTS
      (returnType.contains("Observation") && (ComparisonExpressionTranslator.isNumericComparison(expression)));
  }

  public static List<Expression> traverseConjunctions(Expression expression, List<Expression> list, String returnType) throws Exception {
    List<Expression> newList = new ArrayList<>(list);

    if (expression instanceof And) {
      Expression lhs = ((And) expression).getOperand().get(0);
      Expression rhs = ((And) expression).getOperand().get(1);

      newList.addAll(traverseConjunctions(lhs, new ArrayList<>(), returnType));
      newList.addAll(traverseConjunctions(rhs, new ArrayList<>(), returnType));
    } else if (returnType.contains("Observation") && (ComparisonExpressionTranslator.isNumericComparison(expression)) || (expression instanceof In)) {
      // We only support numeric comparison (if Observation) or In expresion in where clause right now (should be easy to add demographic [Age])
      newList.add(expression);
    } else {
      throw new PhemaTranslationException("Unsupported multi-criteria where clause");
    }

    return newList;
  }

  private static List<Expression> findNestedPropertiesOrAliasRefs(Expression expression, List<Expression> propsOrAliasRefs) {
    List<Expression> newList = new ArrayList<>(propsOrAliasRefs);

    if ((expression instanceof Property) || (expression instanceof AliasRef)) {
      newList.add(expression);
    } else if (expression instanceof UnaryExpression) {
      Expression operand = ((UnaryExpression) expression).getOperand();

      newList.addAll(findNestedPropertiesOrAliasRefs(operand, new ArrayList<>()));
    } else if (expression instanceof BinaryExpression) {
      for (Expression expr : ((BinaryExpression) expression).getOperand()) {
        newList.addAll(findNestedPropertiesOrAliasRefs(expr, new ArrayList<>()));
      }
    } else if (expression instanceof TernaryExpression) {
      for (Expression expr : ((TernaryExpression) expression).getOperand()) {
        newList.addAll(findNestedPropertiesOrAliasRefs(expr, new ArrayList<>()));
      }
    } else if (expression instanceof NaryExpression) {
      for (Expression expr : ((NaryExpression) expression).getOperand()) {
        newList.addAll(findNestedPropertiesOrAliasRefs(expr, new ArrayList<>()));
      }
    } else if (expression instanceof FunctionRef) {
      for (Expression expr : ((FunctionRef) expression).getOperand()) {
        newList.addAll(findNestedPropertiesOrAliasRefs(expr, new ArrayList<>()));
      }
    }

    return newList;
  }

  public static boolean multiCriteriaConjunctionInSameScope(Expression expression, String returnType, String scope) throws Exception {
    // We can only support conjunctions due to the Circe model
    // Disjunctions would have to be lifted outside of the `where` clause
    if (!(expression instanceof And)) {
      return false;
    }

    // Try to get all the criteria that must apply
    List<Expression> criteria;
    try {
      criteria = traverseConjunctions(expression, new ArrayList<>(), returnType);
    } catch (Exception e) {
      return false;
    }

    // Check that all where expressions reference the enclosing retrieve alias at least once
    // Could be on the lhs or rhs
    for (Expression expr : criteria) {
      Expression lhs = ((BinaryExpression) expr).getOperand().get(0);
      Expression rhs = ((BinaryExpression) expr).getOperand().get(1);

      List<Expression> maybeScopedExprs = findNestedPropertiesOrAliasRefs(lhs, new ArrayList<>());
      maybeScopedExprs.addAll(findNestedPropertiesOrAliasRefs(rhs, new ArrayList<>()));

      List<Expression> scopedExprs = maybeScopedExprs.stream().filter(ex -> {
        if (ex instanceof Property) {
          return ((Property) ex).getScope().equals(scope);
        } else if (ex instanceof AliasRef) {
          return ((AliasRef) ex).getName().equals(scope);
        }

        return false;
      }).collect(Collectors.toList());

      if (scopedExprs.size() == 0) {
        return false;
      }
    }

    return true;
  }

  private static CorelatedCriteria applyInCriteria(CorelatedCriteria target, In in) throws Exception {
    // TODO: Address simplifying assumptions noted below
    //   1. Only set window around start time.  Should use property to determine if this is the start or
    //      end time property and identify in the appropriate window.
    //   2. Only handle one interval per correlated query.  In Circe, you can define "event starts" and
    //      "event ends" constraints, and this could also be represented in CQL if we have multiple
    //      constraints.
    List<Expression> whereOperands = in.getOperand();
    if (whereOperands.size() == 2 && whereOperands.get(1) instanceof Interval) {
      Interval interval = (Interval) whereOperands.get(1);

      BinaryExpression lowExpression;
      BinaryExpression highExpression;

      // This handles the special case of date literals, which the CQL parser
      // wraps in ToDateTime
      if (interval.getLow() instanceof ToDateTime) {
        lowExpression = (BinaryExpression) ((Interval) ((Property) ((ToDateTime) interval.getLow()).getOperand()).getSource()).getLow();
      } else {
        lowExpression = (BinaryExpression) interval.getLow();
      }

      if (interval.getHigh() instanceof ToDateTime) {
        highExpression = (BinaryExpression) ((Interval) ((Property) ((ToDateTime) interval.getHigh()).getOperand()).getSource()).getHigh();
      } else {
        highExpression = (BinaryExpression) interval.getHigh();
      }

      target.startWindow.start = TemporalUtil.calculateWindowEndpoint(lowExpression);
      target.startWindow.end = TemporalUtil.calculateWindowEndpoint(highExpression);
    }

    return target;
  }

  public static CorelatedCriteria generateCorelatedCriteriaForCorrelatedQueryWithWhere(Query query, PhemaElmToOmopTranslatorContext context) throws
    Exception {
    Expression whereExpression = query.getWhere();

    // Get the type that the query is returning
    DataType returnType = ((ListType) query.getSource().get(0).getResultType()).getElementType();

    if (!correlationWhereExpressionSupported(whereExpression, returnType.toString())) {
      throw new CorrelationException(String.format("Unsupported correlation: %s", whereExpression.getClass().getSimpleName()));
    }

    // Push the alias to the context so we can access it in the nested expression
    String alias = context.addAlias(query);

    CorelatedCriteria outerCorrelateCriteria = CirceUtil.defaultCorelatedCriteria();

    if (multiCriteriaConjunctionInSameScope(whereExpression, returnType.toString(), alias)) {
      // Handle multiple criteria on the same domain object, so we have to add them all to the same criteria

      // First generate the criteria
      outerCorrelateCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression((Retrieve) query.getSource().get(0).getExpression(), context);

      // Then apply each criteria in the where clause
      List<Expression> criteria = traverseConjunctions(whereExpression, new ArrayList<>(), returnType.toString());
      for (Expression expr : criteria) {
        if (ComparisonExpressionTranslator.isNumericComparison(expr)) {
          // Assume Measurement if we have a numeric comparison
          ((Measurement) outerCorrelateCriteria.criteria).valueAsNumber = ComparisonExpressionTranslator.generateNumericRangeFromComparisonExpression(expr);
        } else if (expr instanceof In) {
          applyInCriteria(outerCorrelateCriteria, (In) expr);
        } else {
          throw new PhemaTranslationException("Unsupported criteria in multi-criteria where clause");
        }
      }
    } else if (whereExpression instanceof Exists || PhemaElmToOmopTranslator.isBooleanExpression(whereExpression)) {
      // descend into the nested expression
      CriteriaGroup criteriaGroup = CriteriaGroupTranslator.generateCriteriaGroupForExpression(whereExpression, context);

      outerCorrelateCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression((Retrieve) query.getSource().get(0).getExpression(), context);

      outerCorrelateCriteria.criteria.CorrelatedCriteria = criteriaGroup;

    } else if (whereExpression instanceof In) {
      // we are in the leaf expression (right now we only support "in Interval")

      // generate the criteria as if it was a retrieve
      outerCorrelateCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression((Retrieve) query.getSource().get(0).getExpression(), context);

      // then set up the occurrence dates based on the interval
      applyInCriteria(outerCorrelateCriteria, (In) whereExpression);
    } else if (returnType.toString().contains("Observation") && (ComparisonExpressionTranslator.isNumericComparison(whereExpression))) {
      // We are checking an Observation/MEASUREMENT's value

      // Generate retrieve
      outerCorrelateCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression((Retrieve) query.getSource().get(0).getExpression(), context);

      // Add the numeric comparison
      ((Measurement) outerCorrelateCriteria.criteria).valueAsNumber = ComparisonExpressionTranslator.generateNumericRangeFromComparisonExpression(whereExpression);
    }

    // Pop the alias to invalidate it
    context.removeAlias(alias);

    return outerCorrelateCriteria;
  }
}
