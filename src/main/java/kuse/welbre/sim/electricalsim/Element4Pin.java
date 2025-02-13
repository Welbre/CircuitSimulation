package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.Tools;

public abstract class Element4Pin extends Element {
    private Pin pinC;
    private Pin pinD;

    public Element4Pin() {
    }

    public Element4Pin(Pin pinA, Pin pinB, Pin pinC, Pin pinD) {
        super(pinA, pinB);
        this.pinC = pinC;
        this.pinD = pinD;
    }

    public void connect(Pin pinA, Pin pinB, Pin pinC, Pin pinD) {
        super.connect(pinA, pinB);
        this.pinC = pinC;
        this.pinD = pinD;
    }

    public void connectC(Pin pin) {
        this.pinC = pin;
    }

    public void connectD(Pin pin) {
        this.pinD = pin;
    }

    public Pin getPinC() {
        return pinC;
    }

    public Pin getPinD() {
        return pinD;
    }

    @Override
    public Pin[] getPins() {
        return new Pin[]{getPinA(), getPinB(), getPinC(), getPinD()};
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s,%s]: %.2fv, %.2fA, %.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getPropriety(), getProprietySymbol(), 4),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                getPinD() == null ? "gnd" : getPinD().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower()
        );
    }
}
