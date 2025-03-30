package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element4Pin;
import kuse.welbre.sim.electrical.abstractt.MultipleRHSElement;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.util.Random;

/**
 * This element is a current-controlled voltage source (CCVS).<br>
 * The pins A and B are the output pins that the voltage source will be injected, we assume that A > B so, the positive voltage difference is from A To B.<br>
 * The pins C and D are the control pins, the positive current direction is from C to D.<br>
 * A {@link CCVS#r r} param is the amplification constant.<br><br>
 * So the control current from C to D will be amplified my {@link CCVS#r r} and outputted as a voltage in A to B direction
 */
@SuppressWarnings("unused")
public class CCVS extends Element4Pin implements MultipleRHSElement {
    private double r;
    private short addressOutput = (short) new Random().nextInt();
    private short addressInput = (short) new Random().nextInt();
    private double[] current_pointerOutput;
    private double[] current_pointerInput;

    public CCVS() {
    }

    public CCVS(double r) {
        this.r = r;
    }

    /**
     * @param pinA The voltage source output positive(+) pin.
     * @param pinB The voltage source output negative(-) pin.
     * @param pinC The current control input negative(+) pin.
     * @param pinD The current control input positive(-) pin.
     * @param r The amplification value.
     */
    public CCVS(Pin pinA, Pin pinB, Pin pinC, Pin pinD, double r) {
        super(pinA, pinB, pinC, pinD);
        this.r = r;
    }

    @Override
    public double[] getProperties() {
        return new double[]{r};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{""};
    }

    @Override
    public double getCurrent() {
        return current_pointerOutput[0];
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampVoltageSource(getPinC(), getPinD(), addressInput, 0);
        builder.stampVoltageSource(getPinA(), getPinB(), addressOutput, 0);
        builder.stampLHS(addressOutput, addressInput, -r);
    }

    @Override
    public void setAddress(short... address) {
        addressOutput = address[0];
        addressInput = address[1];
    }

    @Override
    public int[] getAddress() {
        return new int[]{addressOutput, addressInput};
    }

    @Override
    public void setValuePointer(double[]... pointer) {
        current_pointerOutput = pointer[0];
        current_pointerInput = pointer[1];
    }

    @Override
    public int getRHSAmount() {
        return 2;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s,%s]: %.2fv, %.2fA, %.2fW Control:%.2fA",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 4),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                getPinD() == null ? "gnd" : getPinD().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower(),
                current_pointerInput[0]
        );
    }
}
