package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

public abstract class ArrayExpression extends Expression {
    int length;

    public int compareTo(Expression expr) {
        // unimplemented
        return 0;
    }
}
