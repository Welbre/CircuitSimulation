package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element4Pin;
import kuse.welbre.sim.electrical.abstractt.RHSElement;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * This element is a voltage-controlled voltage source (VCVS).<br>
 * The pins A and B are the output pins that the voltage source will be injected, we assume that A > B so, the positive voltage difference is from A To B.<br>
 * The pins C and D are the control pins; we assume that C > D so, the positive voltage difference is from C To D.<br>
 * A {@link VCVS#micro micro} param is the amplification constant.<br><br>
 * So the control voltage from C to D will be amplified my {@link VCVS#micro micro} and outputted as a voltage in A to B direction.
 */
@SuppressWarnings("unused")
public class VCVS extends Element4Pin implements RHSElement {
    private double micro;
    private double[] current_pointer;
    private short address = (short) new Random().nextInt();

    public VCVS() {
    }

    public VCVS(double micro) {
        this.micro = micro;
    }

    /**
     * @param pinA The voltage source output positive(+) pin.
     * @param pinB The voltage source output negative(-) pin.
     * @param pinC The voltage control input negative(+) pin.
     * @param pinD The voltage control input positive(-) pin.
     * @param micro The amplification value.
     */
    public VCVS(Circuit.Pin pinA, Circuit.Pin pinB, Circuit.Pin pinC, Circuit.Pin pinD, double micro) {
        super(pinA, pinB, pinC, pinD);
        this.micro = micro;
    }

    @Override
    public double[] getProperties() {
        return new double[]{micro};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{""};
    }

    @Override
    public double getCurrent() {
        return current_pointer[0];
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampVoltageSource(getPinA(), getPinB(), address, 0);
        builder.stampCurrentSource(getPinC(), getPinD(), 0);
        if (getPinC() != null)
            builder.stampLHS(address, getPinC().address, -micro);
        if (getPinD() != null)
            builder.stampLHS(address, getPinD().address, micro);
    }

    @Override
    public void setAddress(short address) {
        this.address = address;
    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public void setValuePointer(double[] pointer) {
        this.current_pointer = pointer;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s,%s]: %.2fv, %.2fA, %.2fW Control: %.2fV",
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
        s.writeDouble(micro);
    }

    @Override
    public void unSerialize(DataInputStream s) throws IOException {
        super.unSerialize(s);
        this.micro = s.readDouble();
    }
}
