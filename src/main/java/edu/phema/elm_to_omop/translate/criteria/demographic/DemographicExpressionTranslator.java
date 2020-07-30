package edu.phema.elm_to_omop.translate.criteria.demographic;

import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import edu.phema.elm_to_omop.translate.util.map.NumericRangeOperatorMap;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.NumericRange;

/**
 * Class responsible for generating Circe DemographicCriteria. Right now we only support simple age comparison
 */
public class DemographicExpressionTranslator {
    private DemographicExpressionTranslator()  {
        super();
    }
    /**
     * Check to see if we have an expression of the form CalculatedAge [comparison operator] Literal
     *
     * @param expression The ELM expression
     * @return True if we have a supported demographic expression, false otherwise
     */
    public static boolean isDemographicExpression(Expression expression) {
        // For now we only hand binary operators related to age
        if (!(expression instanceof BinaryExpression)) {
            return false;
        }

        Expression lhs = ((BinaryExpression) expression).getOperand().get(0);
        Expression rhs = ((BinaryExpression) expression).getOperand().get(1);

        // For now we only support simple boolean operators with CalculateAge compared to a literal: e.g. AgeInYears() >= 18
        return ((lhs instanceof CalculateAge) && (rhs instanceof Literal)) || ((rhs instanceof CalculateAge) && (lhs instanceof Literal));
    }

    /**
     * Creates a DemographicCriteria based on a support ELM expression
     *
     * @param expression The ELM expression
     * @return The DemographicCriteria
     * @throws Exception
     */
    public static DemographicCriteria generateDemographicCriteriaForExpression(Expression expression) throws Exception {
        // We've already checked this in the method above
        BinaryExpression binaryExpression = (BinaryExpression) expression;

        boolean inverted = !(binaryExpression.getOperand().get(0) instanceof CalculateAge);

        CalculateAge calculateAge = inverted
            ? (CalculateAge) binaryExpression.getOperand().get(1)
            : (CalculateAge) binaryExpression.getOperand().get(0);

        Literal literal = inverted
            ? (Literal) binaryExpression.getOperand().get(0)
            : (Literal) binaryExpression.getOperand().get(1);

        if (calculateAge.getPrecision() != DateTimePrecision.YEAR) {
            throw new PhemaNotImplementedException("Age criteria are only support at the YEAR precision");
        }

        if (!literal.getValueType().getLocalPart().equals("Integer")) {
            throw new PhemaNotImplementedException("Ages must be compared against integer values");
        }

        NumericRange numericRange = new NumericRange();

        numericRange.value = Integer.parseInt(literal.getValue());

        numericRange.op = inverted
            ? NumericRangeOperatorMap.inverted.get(binaryExpression.getClass().getName())
            : NumericRangeOperatorMap.natural.get(binaryExpression.getClass().getName());

        DemographicCriteria demographicCriteria = new DemographicCriteria();

        demographicCriteria.age = numericRange;

        return demographicCriteria;
    }
}
