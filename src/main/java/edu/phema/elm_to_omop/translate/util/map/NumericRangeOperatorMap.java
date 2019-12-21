package edu.phema.elm_to_omop.translate.util.map;

import edu.phema.elm_to_omop.helper.CirceConstants;
import org.hl7.elm.r1.*;

import java.util.HashMap;
import java.util.Map;

public class NumericRangeOperatorMap {
    public static Map<String, String> natural = new HashMap<String, String>() {{
        put(Less.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN.toString());
        put(LessOrEqual.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN_OR_EQUAL.toString());
        put(Equal.class.getName(), CirceConstants.NumericRangeOperator.EQUAL.toString());
        put(NotEqual.class.getName(), CirceConstants.NumericRangeOperator.NOT_EQUAL.toString());
        put(Greater.class.getName(), CirceConstants.NumericRangeOperator.GREATER_THAN.toString());
        put(GreaterOrEqual.class.getName(), CirceConstants.NumericRangeOperator.GREATER_THAN_OR_EQUAL.toString());
    }};

    public static Map<String, String> inverted = new HashMap<String, String>() {{
        put(Less.class.getName(), CirceConstants.NumericRangeOperator.GREATER_THAN.toString());
        put(LessOrEqual.class.getName(), CirceConstants.NumericRangeOperator.GREATER_THAN_OR_EQUAL.toString());
        put(Equal.class.getName(), CirceConstants.NumericRangeOperator.EQUAL.toString());
        put(NotEqual.class.getName(), CirceConstants.NumericRangeOperator.NOT_EQUAL.toString());
        put(Greater.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN.toString());
        put(GreaterOrEqual.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN_OR_EQUAL.toString());
    }};
}
