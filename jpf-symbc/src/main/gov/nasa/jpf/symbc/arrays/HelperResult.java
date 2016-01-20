package gov.nasa.jpf.symbc.arrays;

import gov.nasa.jpf.symbc.heap.HeapNode;

public class HelperResult {
    public HeapNode n;
    public int idx;

    public HelperResult(HeapNode n, int idx) {
        this.n = n;
        this.idx = idx;
    }
}


