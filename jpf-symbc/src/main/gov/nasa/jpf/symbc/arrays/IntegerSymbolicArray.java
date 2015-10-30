package gov.nasa.jpf.symbc.arrays;

import java.util.Map;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;

public class IntegerSymbolicArray extends ArrayExpression {
    private int length;
    private String name;
    public String solution = "UNDEFINED";


    public IntegerSymbolicArray(int size) {
        super();
        length = size;
    }

    public IntegerSymbolicArray(int n, String name) {
        super();
        name = name;
        length = n;
    }

    public int __length() {
        return length;
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
