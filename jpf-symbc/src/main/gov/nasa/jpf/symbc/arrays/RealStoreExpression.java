package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.ConstraintExpressionVisitor;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.RealExpression;
import gov.nasa.jpf.symbc.numeric.RealConstant;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerConstant;

import java.util.Map;

public class RealStoreExpression extends Expression {
    public ArrayExpression ae;
    public IntegerExpression index;
    public RealExpression value;

    public RealStoreExpression(ArrayExpression ae, IntegerExpression ie, RealExpression value) {
        this.ae = ae;
        this.index = ie;
        this.value = value;
    }

    public RealStoreExpression(ArrayExpression ae, int index, RealExpression value) {
        this(ae, new IntegerConstant(index), value);
    }

    public RealStoreExpression(ArrayExpression ae, IntegerExpression ie, float value) {
        this(ae, ie, new RealConstant(value));
    }

    public RealStoreExpression(ArrayExpression ae, int index, float value) {
        this(ae, new IntegerConstant(index), new RealConstant(value));
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
        return ("store "+ae.stringPC() + " " + index.stringPC() + " " + value.stringPC());
    }

}
