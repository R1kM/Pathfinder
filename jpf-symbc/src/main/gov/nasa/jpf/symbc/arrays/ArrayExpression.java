package gov.nasa.jpf.symbc.arrays;


import gov.nasa.jpf.symbc.arrays.SymbolicIntegerValueAtIndex;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public abstract class ArrayExpression extends Expression {
    public IntegerExpression length;
    public Map<String, SymbolicIntegerValueAtIndex> valAt = null;
    // used for the store operation. We create a new array expression each time we have a store

    public int compareTo(Expression expr) {
        // unimplemented
        return 0;
    }

    public SymbolicIntegerValueAtIndex getVal(IntegerExpression index) {
        if (valAt == null) {
            valAt = new HashMap<String, SymbolicIntegerValueAtIndex>();
        }
        SymbolicIntegerValueAtIndex result = valAt.get(index.toString());
        if (result == null) {
            result = new SymbolicIntegerValueAtIndex(this, index); 
            valAt.put(index.toString(), result);
        }
        return result;
    }

    public SymbolicIntegerValueAtIndex getBoolVal(IntegerExpression index) {
        if (valAt == null) {
            valAt = new HashMap<String, SymbolicIntegerValueAtIndex>();
        }
        SymbolicIntegerValueAtIndex result = valAt.get(index.toString());
        if (result == null) {
            result = new SymbolicIntegerValueAtIndex(this, index, true);
            valAt.put(index.toString(), result);
        }
        return result;
    }

    public void setVal(IntegerExpression index, SymbolicIntegerValueAtIndex value) {
        if (valAt == null) {
            valAt = new HashMap<String, SymbolicIntegerValueAtIndex>();
        }
        String indexName = "";
        // If we have a name for the index, we put it in the map. Else, we create one
        if (index instanceof SymbolicInteger) {
            SymbolicInteger aux = (SymbolicInteger)index;
            indexName = aux.getName();
        }
        else {
            indexName = "ValueAt(INT_"+hashCode()+")";
        }
        // We put the value in the map
        valAt.put(indexName, value);
    }

    public void printValAt() {
        if (valAt == null) {
            System.out.println("valAt is null");
            return;
        }
        for (Map.Entry<String, SymbolicIntegerValueAtIndex> entry : valAt.entrySet()) {
            String key = entry.getKey();
            SymbolicIntegerValueAtIndex val = entry.getValue();
            System.out.println(key + " : " + val);
        }
    }
}
