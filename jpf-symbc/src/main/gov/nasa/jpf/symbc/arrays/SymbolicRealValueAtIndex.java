package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.ArrayExpression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicReal;

public class SymbolicRealValueAtIndex  {
    public ArrayExpression ae;
    public IntegerExpression index;
    public RealExpression value;

    public SymbolicRealValueAtIndex(ArrayExpression ae, IntegerExpression index) {
        RealExpression value = new SymbolicReal("ValueAt("+index.toString()+")");
        this.value = value;
        this.ae = ae;
        this.index = index;
    }

    public SymbolicRealValueAtIndex(ArrayExpression ae, IntegerExpression index, RealExpression value) {
        this.ae = ae;
        this.index = index;
        this.value = value;
    }

    public SymbolicRealValueAtIndex(ArrayExpression ae, IntegerExpression index, boolean isBool) {
        if (isBool) {
         RealExpression value = new SymbolicReal("ValueAt("+index.toString()+")", 0, 1);
         this.value = value;
         this.ae = ae;
         this.index = index;
        }
        else {
        RealExpression value = new SymbolicReal("ValueAt("+index.toString()+")");
        this.value = value;
        this.ae = ae;
        this.index = index;
        }
    }
}
