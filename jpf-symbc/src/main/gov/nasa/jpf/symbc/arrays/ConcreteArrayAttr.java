package gov.nasa.jpf.symbc.arrays;

public class ConcreteArrayAttr {
    private int slot;

    public ConcreteArrayAttr(int slot) {
        this.slot = slot;
    }
 
    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

}
