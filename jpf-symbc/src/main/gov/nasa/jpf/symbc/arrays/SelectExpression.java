package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;

import java.util.Map;

public class SelectExpression extends Expression {
    public ArrayExpression ae;
    public IntegerExpression index;

    public SelectExpression(ArrayExpression ae, IntegerExpression ie) {
        this.ae = ae;
        this.index = ie;
    }

    public SelectExpression(ArrayExpression ae, int index) {
        this.ae = ae;
        this.index = new IntegerConstant(index);
    }

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
        return ("select "+ae.stringPC() + " " + index.stringPC());
    }

}
