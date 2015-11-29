package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.Comparator;

public class ArrayConstraint extends Constraint {
    public ArrayConstraint(SelectExpression se, Comparator c, ArrayExpression ae) {
        super(se, c, ae);
    }

    public ArrayConstraint(StoreExpression se, Comparator c, ArrayExpression ae) {
        super(se, c, ae);
    }

    public ArrayExpression getRight() {
        return (ArrayExpression) super.getRight();
    }

    

    @Override
    public ArrayConstraint not() {
        try {
            return new ArrayConstraint((SelectExpression)super.getLeft(), getComparator().not(), getRight());
        } catch (Exception e) {
            try {
                return new ArrayConstraint((StoreExpression)super.getLeft(), getComparator().not(), getRight());
            }
            catch (Exception r) {
                throw new RuntimeException("ArrayConstraint is not select or store");
            }
        }
    }
}
