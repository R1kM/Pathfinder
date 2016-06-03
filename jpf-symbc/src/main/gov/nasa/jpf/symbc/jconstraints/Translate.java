package gov.nasa.jpf.symbc.jconstraints;

import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.constraints.expressions.CastExpression;
import gov.nasa.jpf.constraints.expressions.Constant;
import gov.nasa.jpf.constraints.types.BuiltinTypes;
import gov.nasa.jpf.constraints.types.Type;

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
    
    public static Expression<Integer> translateInt(Expression<?> symb) {
        Expression<Integer> isymb;
        if (symb.getType().equals(BuiltinTypes.SINT32)) {
            isymb = symb.requireAs(BuiltinTypes.SINT32);
        } else {
            isymb = CastExpression.create(symb, BuiltinTypes.SINT32);
        }
        return isymb;
    }

    public static Expression<Long> translateLong(Expression<?> symb, long value) {
         Expression<Long> isymb;
         if (symb == null) {
             isymb = Constant.create(BuiltinTypes.SINT64, value);
         } else {
             isymb = symb.requireAs(BuiltinTypes.SINT64);
         }
         return isymb;
    }

    public static Expression<Float> translateFloat(Expression<?> symb, float value) {
        Expression<Float> isymb;
        if (symb == null) {
            isymb = Constant.create(BuiltinTypes.FLOAT, value);
        } else {
            isymb = symb.requireAs(BuiltinTypes.FLOAT);
        }
        return isymb;
    }

    public static Expression<Double> translateDouble(Expression<?> symb, double value) {
        Expression<Double> isymb;
        if (symb == null) {
            isymb = Constant.create(BuiltinTypes.DOUBLE, value);
        } else {
            isymb = symb.requireAs(BuiltinTypes.DOUBLE);
        }
        return isymb;
    }

    public static <T> Expression<T> translateIntType(Expression<?> symb, Type<T> type) {
        Expression<T> tsymb = null;
        if (type.equals(symb.getType())) {
            tsymb = symb.requireAs(type);
        } else if (symb instanceof CastExpression) {
            // check for widening/narrowing
            // note that the revers, narrowing/widening is NOT value-preserving!
            CastExpression<?, ?> ce = (CastExpression<?,?>)symb;
            if (ce.getType().equals(BuiltinTypes.SINT32) && ce.getCasted().getType().equals(type))
                tsymb = ce.getCasted().requireAs(type);
        }
        if (tsymb == null) {
            tsymb = CastExpression.create(symb, type);
        }
        return tsymb;
    }
}
