package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.constraints.api.Variable;
import gov.nasa.jpf.constraints.api.Expression;
import gov.nasa.jpf.symbc.heap.HeapNode;
import gov.nasa.jpf.vm.ClassInfo;

public class ArrayHeapNode extends HeapNode {
    // Used to store the index this object was previously loaded from
    public Expression<Integer> arrayIndex = null;
    public int arrayRef = -1;

        public ArrayHeapNode(int idx, ClassInfo tClassInfo, Variable<Integer> sym, Expression<Integer> index, int ref) {
            super(idx, tClassInfo, sym);
            arrayIndex = index;
            arrayRef = ref;
        }
}
