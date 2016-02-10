package gov.nasa.jpf.symbc.arrays;

import java.util.Map;

import gov.nasa.jpf.symbc.arrays.ObjectSymbolicArray;
import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

public class ObjectSymbolicArray extends ArrayExpression {
    private String solution = "UNDEFINED";
    public int slot;
    private String elemType = "?";
    public PreviousObjectArray previous = null;

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

    public ObjectSymbolicArray(PreviousObjectArray previous) {
        super();
        this.length = previous.ae.length;
        String newName = previous.ae.name;
        if (newName.indexOf("!") == -1) {
            newName = newName + "!1";
        } else {
            int aux = Integer.parseInt(newName.substring(newName.indexOf("!") + 1));
            newName = newName.substring(0, newName.indexOf("!") +1) + (aux + 1);
        }
        this.name = newName;
        this.slot = -1;
        this.elemType = previous.ae.elemType;
        this.previous = previous;
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

