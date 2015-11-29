package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.Expression;

import java.util.Map;

public class StoreExpression extends Expression {
    ArrayExpression ae;
    IntegerExpression index;
    IntegerExpression value;

    public int compareTo(Expression expr) {
        // unimplemented
        return 0;
    }

    public void accept(ConstraintExpressionVisitor visitor) {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    public void getVarsVals(Map<String, Object> varsVals) {
        return;
    }

    public String stringPC() {
        return ("store "+ae.stringPC() + " " + index.stringPC() + " " + value.stringPC());
    }

}
