package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class Element3Pin extends Element {
    private Circuit.Pin pinC;

    public Element3Pin() {
        super();
        pinC = new Circuit.Pin();
    }

    public Element3Pin(Circuit.Pin pinA, Circuit.Pin pinB, Circuit.Pin pinC) {
        super(pinA, pinB);
        this.pinC = pinC;
    }

    public void connect(Circuit.Pin pinA, Circuit.Pin pinB, Circuit.Pin pinC) {
        super.connect(pinA, pinB);
        this.pinC = pinC;
    }

    public void connectC(Circuit.Pin pin) {
        this.pinC = pin;
    }

    public Circuit.Pin getPinC() {
        return pinC;
    }

    @Override
    public Circuit.Pin[] getPins() {
        return new Circuit.Pin[]{getPinA(), getPinB(), getPinC()};
    }

    @Override
    public void connect(Circuit.Pin pin, int idx) {
        super.connect(pin, idx);
        if (idx == 2)
            pinC = pin;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s]: %.2fv, %.2fA, %.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 4),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower()
        );
    }

    @Override
    public void serialize(DataOutputStream stream) throws IOException {
        super.serialize(stream);
        stream.writeShort(pinC != null ? pinC.address + 1 : 0);
    }

    @Override
    public void unSerialize(DataInputStream buffer) throws IOException {
        super.unSerialize(buffer);
        short x;
        if ((x = (short) (buffer.readShort()-1)) != -1) {
            this.pinC = new Circuit.Pin(x);
        } else {
            this.pinC = null;
        }
    }
}
