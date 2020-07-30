package edu.phema.elm_to_omop.translate.util.map;

import edu.phema.elm_to_omop.helper.CirceConstants;
import org.hl7.elm.r1.*;

import java.util.HashMap;
import java.util.Map;

public class NumericRangeOperatorMap {
    private NumericRangeOperatorMap()  {
        super();
    }

    public static final Map<String, String> natural = new HashMap<>();

    static {
        natural.put(Less.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN.toString());
        natural.put(LessOrEqual.class.getName(),
            CirceConstants.NumericRangeOperator.LESS_THAN_OR_EQUAL.toString());
        natural.put(Equal.class.getName(), CirceConstants.NumericRangeOperator.EQUAL.toString());
        natural.put(NotEqual.class.getName(),
            CirceConstants.NumericRangeOperator.NOT_EQUAL.toString());
        natural
            .put(Greater.class.getName(),
                CirceConstants.NumericRangeOperator.GREATER_THAN.toString());
        natural.put(GreaterOrEqual.class.getName(),
            CirceConstants.NumericRangeOperator.GREATER_THAN_OR_EQUAL.toString());
    }

    public static final Map<String, String> inverted = new HashMap<>();

    static{
        inverted.put(Less.class.getName(), CirceConstants.NumericRangeOperator.GREATER_THAN.toString());
        inverted.put(LessOrEqual.class.getName(), CirceConstants.NumericRangeOperator.GREATER_THAN_OR_EQUAL.toString());
        inverted.put(Equal.class.getName(), CirceConstants.NumericRangeOperator.EQUAL.toString());
        inverted.put(NotEqual.class.getName(), CirceConstants.NumericRangeOperator.NOT_EQUAL.toString());
        inverted.put(Greater.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN.toString());
        inverted.put(GreaterOrEqual.class.getName(), CirceConstants.NumericRangeOperator.LESS_THAN_OR_EQUAL.toString());
    }
}
