package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class SymbolicIntegerValueAtIndex extends SymbolicInteger {
    ArrayExpression ae;
    IntegerExpression index;
    boolean constant;

    public SymbolicIntegerValueAtIndex(String name, ArrayExpression ae, IntegerExpression index) {
        super(name);
        this.ae = ae;
        this.index = index;
        constant = false;
    }
}
