package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.ObjectSymbolicArray;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

public class PreviousObjectArray {
    ObjectSymbolicArray ae;
    IntegerExpression index;
    IntegerExpression value;

    public PreviousObjectArray(ObjectSymbolicArray ae, IntegerExpression index, IntegerExpression value) {
        this.ae = ae;
        this.index = index;
        this.value = value;
    }
}
