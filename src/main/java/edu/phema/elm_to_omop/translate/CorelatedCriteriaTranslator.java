package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.criteria.CriteriaTranslator;
import edu.phema.elm_to_omop.translate.criteria.comparison.ComparisonExpressionTranslator;
import edu.phema.elm_to_omop.translate.criteria.correlation.CorrelatedQueryTranslator;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Occurrence;

import java.util.List;

/**
 * Class responsible for generating CorelatedCriteria [sic]. CorelatedCriteria criteria are used for applying
 * restrictions to Circe domain Criteria (e.g. ConditionOccurrence).
 * <p>
 * These restrictions include:
 * <p>
 * - start and end window restrictions relative to the index event (the containing Criteria, or the cohort entry event)
 * - occurrence count (e.g. EXACTLY 3 procedures, AT_LEAST 1 condition)
 * - visit restriction relative to the index event (the containing Criteria, or the cohort entry event)
 * <p>
 * Confusingly, a Criteria has field called CorelatedCriteria, which is of type CriteriaGroup.
 */
public class CorelatedCriteriaTranslator {
  private CorelatedCriteriaTranslator() {
    super();
  }

  private static CorelatedCriteria generateCorelatedCriteriaForRetrieve(Retrieve retrieve, PhemaElmToOmopTranslatorContext context) throws Exception {
    Occurrence occurrence = CirceUtil.defaultOccurrence();

    CorelatedCriteria corelatedCriteria = CirceUtil.defaultCorelatedCriteria();
    corelatedCriteria.occurrence = occurrence;

    corelatedCriteria.criteria = CriteriaTranslator.generateCriteriaForExpression(retrieve, context);

    return corelatedCriteria;
  }

  private static CorelatedCriteria generateCorelatedCriteriaForQuery(Query query, PhemaElmToOmopTranslatorContext context) throws Exception {
    List<RelationshipClause> relationships = query.getRelationship();

    if (query.getWhere() == null && (relationships == null || relationships.isEmpty())) {
      // We are dealing with an uncorrelated query

      // Right now we basic just support a simple aliased query like [Condition: "valueset"] C, which is exactly
      // the same a simple retrieve, so we evaluate it as such. If there are no relationships, there should only
      // be one AliasedSource
      return generateCorelatedCriteriaForExpression(query.getSource().get(0).getExpression(), context);
    } else if (relationships.size() == 1) {
      // We are dealing with a correlated query using "such that"
      return CorrelatedQueryTranslator.generateCorelatedCriteriaForCorrelatedQuery(query, context);
    } else if (query.getWhere() != null) {
      // We are dealing with a correlated query using "where"
      return CorrelatedQueryTranslator.generateCorelatedCriteriaForCorrelatedQueryWithWhere(query, context);
    } else {
      throw new PhemaNotImplementedException("The translator is currently only able to handle a single relationship for a data element");
    }
  }

  /**
   * Given an Expression from CQL/ELM, convert it into an OHDSI CorelatedCriteria.
   *
   * @param expression The ELM expression
   * @param context    The ELM translation context
   * @return The CorelatedCriteria for the expression
   * @throws Exception
   */
  public static CorelatedCriteria generateCorelatedCriteriaForExpression(Expression expression, PhemaElmToOmopTranslatorContext context) throws Exception {
    if (expression instanceof Retrieve) {
        return generateCorelatedCriteriaForRetrieve((Retrieve) expression, context);
    } else if (expression instanceof Exists) {
        return generateCorelatedCriteriaForExpression(((Exists) expression).getOperand(), context);
    } else if (expression instanceof Query) {
        return generateCorelatedCriteriaForQuery((Query) expression, context);
    } else if (expression instanceof Not) {
        return ComparisonExpressionTranslator.generateCorelatedCriteriaForExclusion(expression, context);
    } else if (ComparisonExpressionTranslator.isNumericComparison(expression)) {
        return ComparisonExpressionTranslator.generateCorelatedCriteriaForComparison(expression, context);
    } else {
        // TODO - Need to handle more than simple query types
        throw new PhemaNotImplementedException(String.format("Unable to generate CorelatedCriteria for type: %s", expression.getClass().getName()));
    }
  }
}
