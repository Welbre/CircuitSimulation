package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.MatrixBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A linear resistor (R).<br>
 * The pins A and B are where the resistor is connected to.<br>
 * We assume that the voltage in A > voltage in B so, the positive voltage difference is from A to B.<br>
 * Therefore, the positive current direction in from A to B.
 */
@SuppressWarnings("unused")
public class Resistor extends Element {
    private double resistance;
    private double conductance;

    public Resistor() {
        resistance = Double.MAX_VALUE;
        conductance = Double.MIN_VALUE;
    }

    public Resistor(double resistance) {
        setResistance(resistance);
    }

    public Resistor(Pin nodeA, Pin nodeB, double resistance) {
        super(nodeA, nodeB);
        setResistance(resistance);
    }

    public void setResistance(double r){
        resistance = r;
        conductance = 1.0 / r;
    }

    public double getResistance() {
        return resistance;
    }

    public double getConductance() {
        return conductance;
    }

    @Override
    public double getCurrent() {
        return getVoltageDifference() / getResistance();
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampConductance(this.getPinA(), this.getPinB(), this.getConductance());
    }

    @Override
    public double[] getProperties() {
        return new double[]{resistance};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"Ω"};
    }

    @Override
    public void serialize(DataOutputStream stream) throws IOException {
        super.serialize(stream);
        stream.writeDouble(resistance);
    }

    @Override
    public void unSerialize(DataInputStream buffer) throws IOException  {
        super.unSerialize(buffer);
        setResistance(buffer.readDouble());
    }
}
