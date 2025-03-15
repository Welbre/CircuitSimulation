package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Operational;
import kuse.welbre.tools.MatrixBuilder;

@SuppressWarnings("unused")
public class Switch extends Element implements Operational {
    private boolean isOpen = true;
    private boolean dirt = true;
    private double openResistence;//1mΩ default
    private double closedResistence;//1MΩ default

    public Switch() {
        openResistence = 1e-3;
        closedResistence = 1e6;
    }

    public Switch(double openResistence) {
        this.openResistence = openResistence;
    }

    public Switch(double openResistence, boolean isOpen) {
        this.openResistence = openResistence;
        this.isOpen = isOpen;
    }

    public Switch(Pin pinA, Pin pinB) {
        super(pinA, pinB);
        openResistence = 1e-3;
        closedResistence = 1e6;
    }

    public Switch(Pin pinA, Pin pinB, double openResistence, boolean isOpen) {
        super(pinA, pinB);
        this.openResistence = openResistence;
        this.closedResistence = 1e6;
        this.isOpen = isOpen;
    }

    @Override
    public double getQuantity() {
        return openResistence;
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

    public void setClosedResistence(double closedResistence) {
        this.closedResistence = closedResistence;
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
