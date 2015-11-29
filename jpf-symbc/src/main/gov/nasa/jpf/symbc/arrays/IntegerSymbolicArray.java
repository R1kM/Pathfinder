package gov.nasa.jpf.symbc.arrays;

import java.util.Map;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.arrays.PreviousIntegerArray;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;


public class IntegerSymbolicArray extends ArrayExpression {
    private String name;
    public String solution = "UNDEFINED";
    public int slot;
    // Indicates the previous ArrayExpression, as well as the index and value
    // when we store something in the array
    public PreviousIntegerArray previous = null;


    public IntegerSymbolicArray(int size, int slot) {
        super();
        this.length = new IntegerConstant(size);
        this.slot = slot;
    }

    public IntegerSymbolicArray(int n, String name, int slot) {
        super();
        this.name = name;
        this.length = new IntegerConstant(n);
        this.slot = slot;
    }

    public IntegerSymbolicArray(PreviousIntegerArray previous) {
        super();
        this.length = previous.ae.length;
        this.name = previous.ae.name;
        this.slot = previous.ae.slot;
        this.previous = previous;
    }

    public IntegerExpression __length() {
        return length;
    }
    
    public int getSlot() {
        return slot;
    }

    public String getName() {
        return (name!=null) ? name : "ARRAY_" + hashCode();
   }

    public String solution() {
        return solution;
    }

    public String stringPC() {
        return (name != null) ? name : "ARRAY_" + hashCode();
    }

    public void accept(ConstraintExpressionVisitor visitor) {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    public void getVarsVals(Map<String,Object> varsVals) {
        varsVals.put(name, solution);
    }

}
