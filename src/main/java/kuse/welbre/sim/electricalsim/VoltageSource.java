package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.abstractt.Element;
import kuse.welbre.sim.electricalsim.abstractt.RHSElement;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

import java.util.Random;

public class VoltageSource extends Element implements RHSElement {
    private double voltage;
    private double[] current;
    private short address = (short) new Random().nextInt();

    public VoltageSource() {
    }

    public VoltageSource(double voltage) {
        this.voltage = voltage;
    }

    public VoltageSource(Pin nodeA, Pin nodeB, double voltage) {
        super(nodeA, nodeB);
        this.voltage = voltage;
    }

    @Override
    public double getVoltageDifference() {
        return voltage;
    }

    @Override
    public double getCurrent() {
        if (current == null)
            throw new IllegalStateException("Try to get a current in a voltage source that doesn't be initiated!");
        return current[0];
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampVoltageSource(this.getPinA(), this.getPinB(), this.address, this.getVoltageDifference());
    }

    @Override
    protected double getQuantity() {
        return getVoltageDifference();
    }

    @Override
    protected String getQuantitySymbol() {
        return "V";
    }

    @Override
    public void setAddress(short address) {
        this.address = address;
    }

    @Override
    public int getAddress() {
        return this.address;
    }

    @Override
    public void setValuePointer(double[] pointer) {
        this.current = pointer;
    }
}
