package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Operational;
import kuse.welbre.tools.MatrixBuilder;

@SuppressWarnings("unused")
public class Switch extends Element implements Operational {
    private boolean isOpen = true;
    private boolean dirt = true;
    private double closedResistence;//1mΩ default
    private double openResistence;//1MΩ default

    public Switch() {
        closedResistence = 1e-3;
        openResistence = 1e6;
    }

    public Switch(double closedResistence) {
        this.closedResistence = closedResistence;
    }

    public Switch(double closedResistence, boolean isOpen) {
        this.closedResistence = closedResistence;
        this.isOpen = isOpen;
    }

    public Switch(Pin pinA, Pin pinB) {
        super(pinA, pinB);
        closedResistence = 1e-3;
        openResistence = 1e6;
    }

    public Switch(Pin pinA, Pin pinB, double closedResistence, boolean isOpen) {
        super(pinA, pinB);
        this.closedResistence = closedResistence;
        this.openResistence = 1e6;
        this.isOpen = isOpen;
    }

    @Override
    public double getQuantity() {
        return closedResistence;
    }

    @Override
    public String getQuantitySymbol() {
        return "Ω";
    }

    @Override
    public double getCurrent() {
        return getVoltageDifference() / (isOpen ? openResistence : closedResistence);
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        if (isDirt()) {
            builder.stampResistence(getPinA(), getPinB(), isOpen ? openResistence : closedResistence);
            dirt = false;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public double getOpenResistence() {
        return openResistence;
    }

    public double getClosedResistence() {
        return closedResistence;
    }

    public void setOpen(boolean open) {
        isOpen = open;
        dirt();
    }

    public void setOpenResistence(double openResistence) {
        this.openResistence = openResistence;
        dirt();
    }

    public void setCloseResistence(double closeResistence) {
        this.closedResistence = closeResistence;
        dirt();
    }

    @Override
    public boolean isDirt() {
        return dirt;
    }

    @Override
    public void dirt() {
        dirt = true;
    }

    public void toggle(){
        setOpen(!isOpen);
    }
}
