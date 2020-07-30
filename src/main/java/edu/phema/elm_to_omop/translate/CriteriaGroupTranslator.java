package edu.phema.elm_to_omop.translate;

import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.criteria.demographic.DemographicExpressionTranslator;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import org.hl7.elm.r1.And;
import org.hl7.elm.r1.BinaryExpression;
import org.hl7.elm.r1.Expression;
import org.hl7.elm.r1.Or;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;

/**
 * Class responsible for building CriteriaGroups. CriteriaGroups are used to group a potentially heterogeneous
 * collection of CorelatedCriteria, DemographicCriteria, and/or other CriteriaGroups together when you want all or some
 * of them to apply.
 * <p>
 * CriteriaGroups can represent the following types of relationships:
 * <p>
 * - ALL
 * - ANY
 * - AT_LEAST x
 * - AT_MOST x
 * <p>
 * where x is some integer.
 * <p>
 * In CQL, these ideas can be represented using AND and OR statements, or by using the Count aggregate expression.
 */
public class CriteriaGroupTranslator {
    private CriteriaGroupTranslator()  {
        super();
    }

    /**
     * Generates a Circe CriteriaGroup from a ELM BinaryExpression. Creating "ANY" (OR) and "ALL" (AND) CriteriaGroups
     * is how nested boolean logic is implemented. Only these two boolean expressions generated CriteriaGroups. Every
     * other type of ELM expression generates a CorelatedCriteria
     *
     * @param expression The ELM BinaryExpression (must be And or Or)
     * @param context    The translation context
     * @return The created "ANY" or "ALL" CriteriaGroup
     * @throws Exception
     */
    private static CriteriaGroup generateCriteriaGroupForBinaryExpression(BinaryExpression expression, PhemaElmToOmopTranslatorContext context) throws Exception {
        CriteriaGroup criteriaGroup = new CriteriaGroup();
        criteriaGroup.type = PhemaElmToOmopTranslator.getInclusionExpressionType(expression).toString();

        for (Expression operand : expression.getOperand()) {
            Expression operandExp = operand;

            if (DemographicExpressionTranslator.isDemographicExpression(operandExp)) {
                DemographicCriteria demographicCriteria = DemographicExpressionTranslator.generateDemographicCriteriaForExpression(operandExp);

                criteriaGroup.demographicCriteriaList = CirceUtil.addDemographicCriteria(criteriaGroup.demographicCriteriaList, demographicCriteria);
            } else {
                // Are we nesting even further?
                if (PhemaElmToOmopTranslator.isBooleanExpression(operandExp)) {
                    criteriaGroup.groups = CirceUtil.addCriteriaGroup(criteriaGroup.groups, generateCriteriaGroupForBinaryExpression((BinaryExpression) operandExp, context));
                } else {
                    CorelatedCriteria corelatedCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression(operandExp, context);
                    if (corelatedCriteria == null) {
                        throw new PhemaNotImplementedException("The translator was unable to process this type of expression");
                    }
                    criteriaGroup.criteriaList = CirceUtil.addCorelatedCriteria(criteriaGroup.criteriaList, corelatedCriteria);
                }
            }
        }

        return criteriaGroup;
    }

    /**
     * This is the entry point for CriteriaGroup generation. This method will delegate CriteriaGroup generation
     * based on the type of expression. An exception will be thrown if we do not support translating the expression
     *
     * @param expression The ELM expression
     * @param context    The translation context
     * @return The created CriteriaGroup
     * @throws Exception
     */
    public static CriteriaGroup generateCriteriaGroupForExpression(Expression expression, PhemaElmToOmopTranslatorContext context) throws Exception {
        Expression expr = PhemaElmToOmopTranslator.invertNot(expression);

        CriteriaGroup criteriaGroup = new CriteriaGroup();

        if (DemographicExpressionTranslator.isDemographicExpression(expr)) {
            DemographicCriteria demographicCriteria = DemographicExpressionTranslator.generateDemographicCriteriaForExpression(expr);
            criteriaGroup.demographicCriteriaList = CirceUtil.addDemographicCriteria(criteriaGroup.demographicCriteriaList, demographicCriteria);
        } else if (expr instanceof And || expr instanceof Or) {
            return generateCriteriaGroupForBinaryExpression((BinaryExpression) expr, context);
        } else {
            CorelatedCriteria corelatedCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression(expr, context);
            criteriaGroup.criteriaList = CirceUtil.addCorelatedCriteria(criteriaGroup.criteriaList, corelatedCriteria);
        }

        return criteriaGroup;
    }
}
