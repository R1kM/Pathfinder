package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Comparator;
import gov.nasa.jpf.symbc.numeric.RealExpression;

public class RealArrayConstraint extends Constraint {
    public RealArrayConstraint(SelectExpression se, Comparator c, RealExpression ae) {
        super(se, c, ae);
    }

    public RealArrayConstraint(RealStoreExpression se, Comparator c, ArrayExpression ae) {
        super(se, c, ae);
    }

    public RealArrayConstraint not() {
        try {
            return new RealArrayConstraint((SelectExpression)super.getLeft(), getComparator().not(), (RealExpression)getRight());
        } catch (Exception e) {
            try {
                return new RealArrayConstraint((RealStoreExpression)super.getLeft(), getComparator().not(), (ArrayExpression)getRight());
            } catch (Exception r) {
                throw new RuntimeException("ArrayConstraint is not select or store");
            }
        }
    }
}

