package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element4Pin;
import kuse.welbre.sim.electrical.abstractt.RHSElement;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.util.Random;

/**
 * This element is a current-controlled current source (CCCS).<br>
 * The pins A and B are the output pins that the current source will flow,the positive current direction is from A to B.<br>
 * The pins C and D are the control pins, the positive current direction is from C to D.<br>
 * A {@link CCCS#alpha alpha} param is the amplification constant.<br><br>
 * So the control current from C to D will be amplified my {@link CCCS#alpha alpha} and outputted in A to B direction.
 */
@SuppressWarnings("unused")
public class CCCS extends Element4Pin implements RHSElement {
    private double alpha;
    private double[] current;
    private short address = (short) new Random().nextInt();

    public CCCS() {
    }

    public CCCS(double alpha) {
        this.alpha = alpha;
    }

    /**
     * @param pinA The current source output positive(+) pin.
     * @param pinB The current source output negative(-) pin.
     * @param pinC The current control input negative(+) pin.
     * @param pinD The current control input positive(-) pin.
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
            builder.stampLHS(getPinA().address, address, alpha);
        if (getPinB() != null)
            builder.stampLHS(getPinB().address, address, -alpha);
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
        this.current = pointer;
    }

    @Override
    public double getQuantity() {
        return alpha;
    }

    @Override
    public String getQuantitySymbol() {
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
