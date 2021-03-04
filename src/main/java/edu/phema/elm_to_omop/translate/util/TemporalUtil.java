package edu.phema.elm_to_omop.translate.util;

import edu.phema.elm_to_omop.translate.exception.PhemaAssumptionException;
import edu.phema.elm_to_omop.translate.exception.PhemaNotImplementedException;
import org.hl7.elm.r1.*;
import org.ohdsi.circe.cohortdefinition.Window;

import java.math.BigDecimal;
import java.util.List;

/**
 * Helper temporal methods
 */
public class TemporalUtil {
  private TemporalUtil() {
    super();
  }

  static final BigDecimal DAYS_IN_YEAR = BigDecimal.valueOf(365);
  static final BigDecimal DAYS_IN_MONTH = BigDecimal.valueOf(30);
  static final BigDecimal DAYS_IN_WEEK = BigDecimal.valueOf(7);

  public static Window.Endpoint calculateWindowEndpoint(BinaryExpression expression) throws PhemaNotImplementedException, PhemaAssumptionException {
    if (expression instanceof Subtract || expression instanceof Add) {
      Quantity quantity = (Quantity)getExpressionOfType(expression.getOperand(), Quantity.class);
      if (quantity == null) {
        throw new PhemaAssumptionException("We expected a quantity to be specified in the relationship, but none was found");
      }

      Window.Endpoint start = new Window().new Endpoint();
      start.coeff = (expression instanceof Subtract) ? -1 : 1;
      start.days = convertToDays(quantity).intValue();

      return start;
    }

    throw new PhemaNotImplementedException("The translator currently only supports Subtract and Add operations");
  }

  public static BigDecimal convertToDays(Quantity quantity) throws PhemaAssumptionException, PhemaNotImplementedException {
    if (quantity == null) {
      throw new PhemaAssumptionException("The expected quantity is null");
    }

    if (quantity.getValue() == null || quantity.getUnit() == null || quantity.getUnit().equals("")) {
      throw new PhemaAssumptionException("The quantity must contain both a value and a unit");
    }

    String unit = quantity.getUnit().toLowerCase();
    BigDecimal value = quantity.getValue();
    if (unit.equals("year") || unit.equals("years")) {
      value = value.multiply(DAYS_IN_YEAR);
    } else if (unit.equals("month") || unit.equals("months")) {
      value = value.multiply(DAYS_IN_MONTH);
    } else if (unit.equals("week") || unit.equals("weeks")) {
      value = value.multiply(DAYS_IN_WEEK);
    } else if (unit.equals("day") || unit.equals("days")) {
      // No conversion needed
    } else {
      throw new PhemaNotImplementedException("The translator doesn't translate this unit");
    }

    return value;
  }

  private static Expression getExpressionOfType(List<Expression> list, Class type) {
    for (Expression expr : list) {
      if (expr.getClass().equals(type)) {
        return expr;
      }
    }

    return null;
  }


}
