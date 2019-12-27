package edu.phema.elm_to_omop.translate.criteria.comparison;

import edu.phema.elm_to_omop.helper.CirceUtil;
import edu.phema.elm_to_omop.translate.CorelatedCriteriaTranslator;
import edu.phema.elm_to_omop.translate.PhemaElmToOmopTranslatorContext;
import edu.phema.elm_to_omop.translate.exception.PhemaTranslationException;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.Occurrence;

import java.text.NumberFormat;
import java.text.ParseException;

public class ComparisonExpressionTranslator {
    private static class ComparisonExpressionValuePair {
        private Expression expression;
        private Number value;

        public static ComparisonExpressionValuePair from(BinaryExpression binaryExpression) throws PhemaTranslationException {
            ComparisonExpressionValuePair result = new ComparisonExpressionValuePair();

            result.expression = binaryExpression;

            // We are assuming there are 2 operands to build an occurrence.  If that's violated, we throw an exception.  At
            // that point we'll need to revisit what to do to expand our assumptions.
            boolean hasCount = false;
            String countString = null;

            for (Expression operand : binaryExpression.getOperand()) {
                if (operand instanceof Count) {
                    hasCount = true;
                } else if (operand instanceof Literal) {
                    countString = ((Literal) operand).getValue();
                }
            }
            if (!hasCount || countString == null) {
                throw new PhemaTranslationException("The translator expected an expression with a Count and Literal operand, but these were not found.");
            }

            try {
                result.value = NumberFormat.getInstance().parse(countString);
            } catch (ParseException pe) {
                throw new PhemaTranslationException(String.format("Error parsing comparision value %s", countString));
            }

            return result;
        }

        public Expression getExpression() {
            return expression;
        }

        public Number getValue() {
            return value;
        }
    }

    /**
     * Tests a given expression to determine if we support it as a comparison expression
     *
     * @param expression The expression
     * @return True if we support it, false otherwise
     */
    public static boolean isNumericComparison(Expression expression) {
        return (expression instanceof Greater) ||
            (expression instanceof GreaterOrEqual) ||
            (expression instanceof Equal) ||
            (expression instanceof Less) ||
            (expression instanceof LessOrEqual);
    }

    /**
     * Right now we only support the simple comparison of the form Count(X) > y, where X is a Retrieve or Query
     * expression and y is a number.
     *
     * @param expression The comparison expression
     * @param context    The translation context
     * @return The created CorelatedCriteria
     * @throws Exception
     */
    public static CorelatedCriteria generateCorelatedCriteriaForComparison(Expression expression, PhemaElmToOmopTranslatorContext context) throws Exception {
        Occurrence occurrence = getNumericComparisonOccurrence((BinaryExpression) expression);

        Expression comparisonSource = getNumericComparisonSourceExpression(expression);

        CorelatedCriteria corelatedCriteria = CorelatedCriteriaTranslator.generateCorelatedCriteriaForExpression(comparisonSource, context);

        corelatedCriteria.occurrence = occurrence;

        return corelatedCriteria;
    }

    /**
     * Gets the data source for a comparison expression. Right now this will be a Retrieve or a Query, which is will
     * be the source expression for a Count
     *
     * @param comparisonExpression The comparison expression
     * @return The Retrieve of Query source
     * @throws PhemaTranslationException
     */
    public static Expression getNumericComparisonSourceExpression(Expression comparisonExpression) throws PhemaTranslationException {
        if (!isNumericComparison(comparisonExpression)) {
            throw new PhemaTranslationException(String.format("Unsupported comparison operation: s", comparisonExpression.getClass().getSimpleName()));
        }

        BinaryExpression expression = (BinaryExpression) comparisonExpression;

        Expression nonLiteralOperand = expression
            .getOperand()
            .stream()
            .filter(e -> !(e instanceof Literal))
            .findFirst()
            .orElseThrow(() -> new PhemaTranslationException(String.format("Expected %s to have a non-Literal operand", expression.getClass().getSimpleName())));

        if (nonLiteralOperand instanceof Count) {
            return ((Count) nonLiteralOperand).getSource();
        } else {
            throw new PhemaTranslationException(String.format("Unsupported comparison operand: %s", nonLiteralOperand.getClass().getSimpleName()));
        }
    }

    /**
     * Helper method to extract the Occurrence information from a BinaryExpression.  This is assuming that the
     * BinaryExpression is of a type that contains a Count (e.g., Greater, Less).
     *
     * @param binaryExpression
     * @return
     * @throws Exception
     */
    public static Occurrence getNumericComparisonOccurrence(BinaryExpression binaryExpression) throws Exception {
        ComparisonExpressionValuePair pair = ComparisonExpressionValuePair.from(binaryExpression);

        Occurrence occurrence = CirceUtil.defaultOccurrence();
        occurrence.count = pair.getValue().intValue();

        Expression expression = pair.getExpression();
        if (expression instanceof Greater) {
            occurrence.type = Occurrence.AT_LEAST;
            // Because OHDSI uses "at least" (which is >=), we adjust the count value for equivalency
            occurrence.count++;
        } else if (expression instanceof GreaterOrEqual) {
            occurrence.type = Occurrence.AT_LEAST;
        } else if (expression instanceof Equal) {
            occurrence.type = Occurrence.EXACTLY;
        } else if (expression instanceof Less) {
            occurrence.type = Occurrence.AT_MOST;
            // Because OHDSI uses "at most" (which is <=), we adjust the count value for equivalency
            occurrence.count--;
        } else if (expression instanceof LessOrEqual) {
            occurrence.type = Occurrence.AT_MOST;
        }

        // We want to default to counting distinct occurrences
        occurrence.isDistinct = true;

        return occurrence;
    }
}
