package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

public class SymbolicIntegerValueAtIndex extends SymbolicInteger {
    IntegerExpression ie;
    IntegerExpression index;
    boolean constant;

}
