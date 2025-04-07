package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Operational;
import kuse.welbre.tools.MatrixBuilder;

import java.nio.ByteBuffer;

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
        this.openResistence = 1e6;
    }

    public Switch(double closedResistence, double openResistence) {
        this.closedResistence = closedResistence;
        this.openResistence = openResistence;
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
    public double[] getProperties() {
        return new double[]{closedResistence, openResistence};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"Ω","Ω"};
    }

    @Override
    public double getCurrent() {
        //only use isOpen start if isn't dirt
        return getVoltageDifference() / (isOpen ^ dirt ? openResistence : closedResistence);
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampResistence(getPinA(), getPinB(), isOpen ? openResistence : closedResistence);
        dirt = false;
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

    @Override
    public void serialize(ByteBuffer buffer) {
        super.serialize(buffer);
        buffer.putDouble(openResistence).putDouble(closedResistence).put(isOpen ? (byte) 0xff : 0);
    }

    @Override
    public void unSerialize(ByteBuffer buffer) {
        super.unSerialize(buffer);
        this.openResistence = buffer.getDouble();
        this.closedResistence = buffer.getDouble();
        this.isOpen = buffer.get() == (byte) 0xff;
    }
}
