package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.arrays.ArrayExpression;

public class ArrayConstraint {
    ArrayExpression left;

    ArrayComparator comp;

    ArrayExpression right;

    ArrayConstraint and;

    ArrayConstraint(ArrayExpression l, ArrayComparator c, ArrayExpression r) {
        left = l;
        comp = c;
        right = r;
    }

}
