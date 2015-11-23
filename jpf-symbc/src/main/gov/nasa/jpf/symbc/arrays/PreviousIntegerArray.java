package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.IntegerSymbolicArray;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

public class PreviousIntegerArray {
    IntegerSymbolicArray ae;
    IntegerExpression index;
    IntegerExpression value;

    public PreviousIntegerArray(IntegerSymbolicArray ae, IntegerExpression index, IntegerExpression value) {
        this.ae = ae;
        this.index = index;
        this.value = value;
    }
}
