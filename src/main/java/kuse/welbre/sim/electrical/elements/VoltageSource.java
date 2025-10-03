package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.RHSElement;
import kuse.welbre.tools.MatrixBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public VoltageSource(Circuit.Pin nodeA, Circuit.Pin nodeB, double voltage) {
        super(nodeA, nodeB);
        this.voltage = voltage;
    }

    @Override
    public double getVoltageDifference() {
        return voltage;
    }

    public double getSourceVoltage()
    {
        return voltage;
    }

    public void setSourceVoltage(double voltage) {
        this.voltage = voltage;
    }

    @Override
    public double getCurrent() {
        if (current == null)
            return Double.NaN;
        return current[0];
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampVoltageSource(this.getPinA(), this.getPinB(), this.address, this.getVoltageDifference());
    }

    @Override
    public double[] getProperties() {
        return new double[]{getVoltageDifference()};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"V"};
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

    @Override
    public void serialize(DataOutputStream s) throws IOException {
        super.serialize(s);
        s.writeDouble(voltage);
    }

    @Override
    public void unSerialize(DataInputStream s) throws IOException{
        super.unSerialize(s);
        this.setSourceVoltage(s.readDouble());
    }
}
