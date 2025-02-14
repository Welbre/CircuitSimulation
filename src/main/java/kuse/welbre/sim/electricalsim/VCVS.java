package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.abstractt.Element;
import kuse.welbre.sim.electricalsim.abstractt.Element4Pin;
import kuse.welbre.sim.electricalsim.abstractt.RHSElement;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;
import kuse.welbre.sim.electricalsim.tools.Tools;

import java.util.Random;

public class VCVS extends Element4Pin implements RHSElement {
    private double micro;
    private double[] current_pointer;
    private short address = (short) new Random().nextInt();

    public VCVS() {
    }

    public VCVS(double micro) {
        this.micro = micro;
    }

    public VCVS(Pin pinA, Pin pinB, Pin pinC, Pin pinD, double micro) {
        super(pinA, pinB, pinC, pinD);
        this.micro = micro;
    }

    @Override
    protected double getQuantity() {
        return micro;
    }

    @Override
    protected String getQuantitySymbol() {
        return "";
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
            builder.stampGRaw(address, getPinC().address, -micro);
        if (getPinD() != null)
            builder.stampGRaw(address, getPinD().address, micro);
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
                Tools.proprietyToSi(getQuantity(), getQuantitySymbol(), 4),
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
}
