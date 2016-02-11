package gov.nasa.jpf.symbc.arrays;

import java.util.Map;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.arrays.PreviousIntegerArray;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;


public class IntegerSymbolicArray extends ArrayExpression {
    public String solution = "UNDEFINED";
    // Indicates the previous ArrayExpression, as well as the index and value
    // when we store something in the array
    public PreviousIntegerArray previous = null;


    public IntegerSymbolicArray(int size) {
        super();
        this.length = new IntegerConstant(size);
    }

    public IntegerSymbolicArray(int n, String name) {
        super();
        this.name = name;
        this.length = new IntegerConstant(n);
    }

    public IntegerSymbolicArray(IntegerExpression n, String name) {
        super();
        this.name = name;
        this.length = n;
    }

    public IntegerSymbolicArray(PreviousIntegerArray previous) {
        super();
        this.length = previous.ae.length;
        String newName = previous.ae.name;
        if (newName.indexOf("!") == -1) {
            newName = newName+ "!1";
        } else {
            int aux = Integer.parseInt(newName.substring(newName.indexOf("!") + 1));
            newName = newName.substring(0, newName.indexOf("!") +1) + (aux + 1);
        }
        this.name = newName;
        this.previous = previous;
    }

    public IntegerExpression __length() {
        return length;
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
