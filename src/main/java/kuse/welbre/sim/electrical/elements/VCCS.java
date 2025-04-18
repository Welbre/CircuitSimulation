package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element4Pin;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This element is a voltage-controlled current source (VCCS).<br>
 * The pins A and B are the output pins that the current source will flow,the positive current direction is from A to B.<br>
 * The pins C and D are the control pins, we assume that C > D so, the positive voltage difference is from C To D.<br>
 * A {@link VCCS#g g} param is the amplification constant.<br><br>
 * So the control voltage from C to D will be amplified my {@link VCCS#g g} and outputted as a current in A to B direction.
 */
@SuppressWarnings("unused")
public class VCCS extends Element4Pin {
    private double g;

    public VCCS() {
    }

    public VCCS(double g) {
        this.g = g;
    }

    /**
     * @param pinA The current source output positive(+) pin.
     * @param pinB The current source output negative(-) pin.
     * @param pinC The voltage control input positive(+) pin.
     * @param pinD The voltage control input positive(-) pin.
     * @param g The amplification value.
     */
    public VCCS(Circuit.Pin pinA, Circuit.Pin pinB, Circuit.Pin pinC, Circuit.Pin pinD, double g) {
        super(pinA, pinB, pinC, pinD);
        this.g = g;
    }

    @Override
    public double[] getProperties() {
        return new double[]{g};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{""};
    }

    @Override
    public double getCurrent() {
        return Circuit.Pin.GET_VOLTAGE_DIFF(getPinC(), getPinD()) * g;
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        if (getPinA() != null) {
            if (getPinC() != null)
                builder.stampLHS(getPinA().address, getPinC().address, g);
            if (getPinD() != null)
                builder.stampLHS(getPinA().address, getPinD().address, -g);
        }
        if (getPinB() != null) {
            if (getPinC() != null)
                builder.stampLHS(getPinB().address, getPinC().address, -g);
            if (getPinD() != null)
                builder.stampLHS(getPinB().address, getPinD().address, g);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s,%s]: %.2fv, %.2fA, %.2fW Control:%.2fV",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 4),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                getPinD() == null ? "gnd" : getPinD().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower(),
                Circuit.Pin.GET_VOLTAGE_DIFF(getPinC(), getPinD())
        );
    }

    @Override
    public void serialize(DataOutputStream s) throws IOException {
        super.serialize(s);
        s.writeDouble(g);
    }

    @Override
    public void unSerialize(DataInputStream s) throws IOException {
        super.unSerialize(s);
        this.g = s.readDouble();
    }
}
