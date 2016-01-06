package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class SymbolicIntegerValueAtIndex  {
    public ArrayExpression ae;
    public IntegerExpression index;
    public IntegerExpression value;

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

    public SymbolicIntegerValueAtIndex(ArrayExpression ae, IntegerExpression index, boolean isBool) {
        if (isBool) {
         IntegerExpression value = new SymbolicInteger("ValueAt("+index.toString()+")", 0, 1);
         this.value = value;
         this.ae = ae;
         this.index = index;
        }
        else {
        IntegerExpression value = new SymbolicInteger("ValueAt("+index.toString()+")");
        this.value = value;
        this.ae = ae;
        this.index = index;
        }
    }
}
