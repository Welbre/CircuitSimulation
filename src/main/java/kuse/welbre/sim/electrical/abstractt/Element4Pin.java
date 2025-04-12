package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class Element4Pin extends Element3Pin {
    private Circuit.Pin pinD;

    public Element4Pin() {
        super();
        pinD = new Circuit.Pin();
    }

    public Element4Pin(Circuit.Pin pinA, Circuit.Pin pinB, Circuit.Pin pinC, Circuit.Pin pinD) {
        super(pinA, pinB, pinC);
        this.pinD = pinD;
    }

    public void connect(Circuit.Pin pinA, Circuit.Pin pinB, Circuit.Pin pinC, Circuit.Pin pinD) {
        super.connect(pinA, pinB, pinC);
        this.pinD = pinD;
    }

    public void connectD(Circuit.Pin pin) {
        this.pinD = pin;
    }

    public Circuit.Pin getPinD() {
        return pinD;
    }

    @Override
    public Circuit.Pin[] getPins() {
        return new Circuit.Pin[]{getPinA(), getPinB(), getPinC(), getPinD()};
    }

    @Override
    public void connect(Circuit.Pin pin, int idx) {
        super.connect(pin, idx);
        if (idx == 3)
            pinD = pin;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s,%s]: %.2fv, %.2fA, %.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 4),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                getPinD() == null ? "gnd" : getPinD().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower()
        );
    }

    @Override
    public void serialize(DataOutputStream s) throws IOException {
        super.serialize(s);
        s.writeShort(pinD != null ? pinD.address + 1 : 0);
    }

    @Override
    public void unSerialize(DataInputStream s) throws IOException {
        super.unSerialize(s);
        short x;
        this.connectD((x = (short) (s.readShort()-1)) != -1 ? new Circuit.Pin(x) : null);
    }
}
