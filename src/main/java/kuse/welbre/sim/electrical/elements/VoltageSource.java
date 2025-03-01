package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.RHSElement;
import kuse.welbre.tools.MatrixBuilder;

import java.util.Random;

/**
 * A simple voltage source (VS).<br>
 * The pins A and B are the output pins that the voltage source will be injected, we assume that A > B so, the positive voltage difference is from A To B.<br>
 * The current follow the same idea, the positive direction is from A to B.
 */
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

    public void setSourceVoltage(double voltage) {
        this.voltage = voltage;
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
    public double getQuantity() {
        return getVoltageDifference();
    }

    @Override
    public String getQuantitySymbol() {
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
