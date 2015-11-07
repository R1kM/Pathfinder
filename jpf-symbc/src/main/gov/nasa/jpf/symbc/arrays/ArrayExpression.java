package gov.nasa.jpf.symbc.arrays;


import gov.nasa.jpf.symbc.arrays.SymbolicIntegerValueAtIndex;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public abstract class ArrayExpression extends Expression {
    public int length;
    public Map<String, SymbolicIntegerValueAtIndex> valAt = null;

    public int compareTo(Expression expr) {
        // unimplemented
        return 0;
    }

    public IntegerExpression getVal(IntegerExpression index) {
        if (valAt == null) {
            valAt = new HashMap<String, SymbolicIntegerValueAtIndex>();
        }
        SymbolicIntegerValueAtIndex result = valAt.get(index.toString());
        if (result == null) {
            result = new SymbolicIntegerValueAtIndex("ValueAt("+index.toString() + ")", this, index); 
            valAt.put(index.toString(), result);
        }
        return result;
    }

    public void printValAt() {
        for (Map.Entry<String, SymbolicIntegerValueAtIndex> entry : valAt.entrySet()) {
            String key = entry.getKey();
            SymbolicIntegerValueAtIndex val = entry.getValue();
            System.out.println(key + " : " + val);
        }
    }
}
