package gov.nasa.jpf.symbc.arrays;

import java.util.Map;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

public class ObjectSymbolicArray extends ArrayExpression {
    private String name;
    private String solution = "UNDEFINED";
    public int slot;
    private String elemType = null;

    public ObjectSymbolicArray(int size, int slot) {
        super();
        this.length = new IntegerConstant(size);
        this.slot = slot;
    }

    public ObjectSymbolicArray(int n, String name, int slot) {
        super();
        this.name = name;
        this.length = new IntegerConstant(n);
        this.slot = slot;
    }

    public ObjectSymbolicArray(IntegerExpression n, String name, int slot, String arrayType) {
        super();
        this.name = name;
        this.length = n;
        this.slot = slot;
        this.elemType = arrayType.substring(0, arrayType.length() - 2); // We remove [] at the end of the arrayType
    }

    public IntegerExpression __length() {
        return length;
    }

    public String getElemType() {
        return elemType;
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return (name != null) ? name : "ARRAY_"+hashCode();
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

    public void getVarsVals(Map<String, Object> varsVals) {
        varsVals.put(name, solution);
    }
}

