package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.heap.HeapNode;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.SymbolicInteger;
import gov.nasa.jpf.vm.ClassInfo;

public class ArrayHeapNode extends HeapNode {
    // Used to store the index this object was previously loaded from
    public IntegerExpression arrayIndex = null;
    public int arrayRef = -1;

        public ArrayHeapNode(int idx, ClassInfo tClassInfo, SymbolicInteger sym, IntegerExpression index, int ref) {
            super(idx, tClassInfo, sym);
            arrayIndex = index;
            arrayRef = ref;
        }
}
