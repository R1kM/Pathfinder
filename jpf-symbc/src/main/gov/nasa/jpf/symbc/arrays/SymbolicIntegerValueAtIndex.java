package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class SymbolicIntegerValueAtIndex  {
    ArrayExpression ae;
    IntegerExpression index;
    IntegerExpression value;

    public SymbolicIntegerValueAtIndex(ArrayExpression ae, IntegerExpression index) {
        IntegerExpression value = new SymbolicInteger("ValueAt("+index.toString()+")");
        this.value = value;
        this.ae = ae;
        this.index = index;
    }

    public SymbolicIntegerValueAtIndex(ArrayExpression ae, IntegerExpression index, IntegerExpression value) {
        this.ae = ae;
        this.index = index;
        this.value = value;
    }
}
