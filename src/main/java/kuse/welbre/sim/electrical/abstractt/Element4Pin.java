package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class Element4Pin extends Element3Pin {
    private Pin pinD;

    public Element4Pin() {
        super();
        pinD = new Pin();
    }

    public Element4Pin(Pin pinA, Pin pinB, Pin pinC, Pin pinD) {
        super(pinA, pinB, pinC);
        this.pinD = pinD;
    }

    public void connect(Pin pinA, Pin pinB, Pin pinC, Pin pinD) {
        super.connect(pinA, pinB, pinC);
        this.pinD = pinD;
    }

    public void connectD(Pin pin) {
        this.pinD = pin;
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
    public void serialize(DataOutputStream stream) throws IOException {
        super.serialize(stream);
        stream.writeShort(pinD != null ? pinD.address + 1 : 0);
    }

    @Override
    public void unSerialize(DataInputStream buffer) throws IOException {
        super.unSerialize(buffer);
        short x;
        if ((x = (short) (buffer.readShort()-1)) != -1) {
            this.pinD = new Pin(x);
        } else {
            this.pinD = null;
        }
    }
}
