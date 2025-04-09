package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.MatrixBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A simple current source (CS).<br>
 * The pins A and B are the output pins that the current source will flow,the positive current direction is from A to B.<br>
 */
@SuppressWarnings("unused")
public class CurrentSource extends Element {
    private double current;

    public CurrentSource() {
    }

    public CurrentSource(double current) {
        this.current = current;
    }

    public CurrentSource(Circuit.Pin nodeA, Circuit.Pin nodeB, double current) {
        super(nodeA, nodeB);
        this.current = current;
    }

    public double getCurrent() {
        return current;
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampCurrentSource(this.getPinA(), this.getPinB(), this.getCurrent());
    }

    @Override
    public double[] getProperties() {
        return new double[]{current};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"A"};
    }

    @Override
    public void serialize(DataOutputStream stream) throws IOException {
        super.serialize(stream);
        stream.writeDouble(current);
    }

    @Override
    public void unSerialize(DataInputStream buffer) throws IOException {
        super.unSerialize(buffer);
        current = buffer.readDouble();
    }
}
