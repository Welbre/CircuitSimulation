package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class Element3Pin extends Element {
    private Pin pinC;

    public Element3Pin() {
        super();
        pinC = new Pin();
    }

    public Element3Pin(Pin pinA, Pin pinB, Pin pinC) {
        super(pinA, pinB);
        this.pinC = pinC;
    }

    public void connect(Pin pinA, Pin pinB, Pin pinC) {
        super.connect(pinA, pinB);
        this.pinC = pinC;
    }

    public void connectC(Pin pin) {
        this.pinC = pin;
    }

    public Pin getPinC() {
        return pinC;
    }

    @Override
    public Pin[] getPins() {
        return new Pin[]{getPinA(), getPinB(), getPinC()};
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
            this.pinC = new Pin(x);
        } else {
            this.pinC = null;
        }
    }
}
