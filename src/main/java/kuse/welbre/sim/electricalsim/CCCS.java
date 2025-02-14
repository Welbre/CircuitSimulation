package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;
import kuse.welbre.sim.electricalsim.tools.Tools;

import java.util.Random;

/**
 * This element is a current-controlled current source (CCCS).<br>
 * The pins A and B are the output pins that the current source will flow,the positive current direction is from A to B.<br>
 * The pins C and D are the control pins, the positive current direction is from D to C.<br>
 * A {@link CCCS#alpha alpha} param is the amplification constant.<br><br>
 * So the control current from D to c will be amplified my {@link CCCS#alpha alpha} and outputted in A to B direction.
 */
@SuppressWarnings("unsued")
public class CCCS extends Element4Pin {
    private double alpha;
    //A pointer to current
    private double[] current;
    //Address in Z matrix.
    public short address = (short) new Random().nextInt();

    public CCCS() {
    }

    public CCCS(double alpha) {
        this.alpha = alpha;
    }

    /**
     * @param pinA The current source output positive(+) pin.
     * @param pinB The current source output negative(-) pin.
     * @param pinC The current control input negative(-) pin.
     * @param pinD The current control input positive(+) pin.
     * @param alpha The amplification value.
     */
    public CCCS(Pin pinA, Pin pinB, Pin pinC, Pin pinD, double alpha) {
        super(pinA, pinB, pinC, pinD);
        this.alpha = alpha;
    }

    @Override
    public double getCurrent() {
        return current[0] * alpha;
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampVoltageSource(getPinC(), getPinD(), address, 0);//A perfect ammeter.
        if (getPinA() != null)
            builder.stampGRaw(getPinA().address, address, alpha);
        if (getPinB() != null)
            builder.stampGRaw(getPinB().address, address, -alpha);
    }

    public void setCurrentPointer(double[] pointer) {
        current = pointer;
    }

    @Override
    protected double getQuantity() {
        return alpha;
    }

    @Override
    protected String getQuantitySymbol() {
        return "";
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s,%s]: %.2fv, %.2fA, %.2fW Control: %.2fA",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getQuantity(), getQuantitySymbol(), 4),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                getPinD() == null ? "gnd" : getPinD().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower(),
                current[0]
        );
    }
}
