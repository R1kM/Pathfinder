package gov.nasa.jpf.symbc.arrays;


import gov.nasa.jpf.symbc.arrays.SymbolicIntegerValueAtIndex;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

import java.util.Map;

public abstract class ArrayExpression extends Expression {
    int length;
    Map<String, SymbolicIntegerValueAtIndex> valAt = null;

    public int compareTo(Expression expr) {
        // unimplemented
        return 0;
    }
}
