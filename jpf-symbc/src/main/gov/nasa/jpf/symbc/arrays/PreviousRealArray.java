package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.RealSymbolicArray;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.RealExpression;

public class PreviousRealArray {
    RealSymbolicArray ae;
    IntegerExpression index;
    RealExpression value;

    public PreviousRealArray(RealSymbolicArray ae, IntegerExpression index, RealExpression value) {
        this.ae = ae;
        this.index = index;
        this.value = value;
    }
}
