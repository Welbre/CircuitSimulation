package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Element4Pin;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.nio.ByteBuffer;

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
    public VCCS(Pin pinA, Pin pinB, Pin pinC, Pin pinD, double g) {
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
        return Element.GET_VOLTAGE_DIFF(getPinC(), getPinD()) * g;
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
                Element.GET_VOLTAGE_DIFF(getPinC(), getPinD())
        );
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        super.serialize(buffer);
        buffer.putDouble(g);
    }

    @Override
    public void unSerialize(ByteBuffer buffer) {
        super.unSerialize(buffer);
        this.g = buffer.getDouble();
    }
}
