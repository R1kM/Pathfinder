package gov.nasa.jpf.symbc.jconstraints;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.types.BuiltinTypes;

public class Translate {
    public static Expression<Integer> translateInt(Expression<?> symb, int value) {
        Expression<Integer> isymb;
        if (symb == null) {
            isymb = Constant.create(BuiltinTypes.SINT32, value);
        } else if (symb.getType().equals(BuiltinTypes.SINT32)) {
            isymb = symb.requireAs(BuiltinTypes.SINT32);
        } else {
            isymb = CastExpression.create(symb, BuiltinTypes.SINT32);
        }
        return isymb;
    }
}
