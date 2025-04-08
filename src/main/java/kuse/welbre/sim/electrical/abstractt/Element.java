package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.CircuitBuilder;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.*;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Element implements Serializable {
    public static final class Pin {
        private static final Random rand = new Random();
        public short address;
        public double[] P_voltage = null;

        public Pin(short node) {
            this.address = node;
        }

        public Pin() {
            address = (short) rand.nextInt();
        }

        @Override
        public String toString() {
            return "Pin[" + address + "]";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Pin pin && pin.address == this.address;
        }

        @Override
        public int hashCode() {
            return address;//todo this is not the ideal, create one system that is fast, light, and can be easily replaced by the current one.
        }
    }
    private Pin pinA;
    private Pin pinB;

    public Element() {
        pinA = new Pin();
        pinB = new Pin();

        CircuitBuilder.HANDLE(this);
    }

    public Element(Pin pinA, Pin pinB) {
        this.pinA = pinA;
        this.pinB = pinB;

        CircuitBuilder.HANDLE(this);
    }

    public Pin getPinA() {
        return pinA;
    }

    public Pin getPinB() {
        return pinB;
    }

    public void connectA(Pin pin) {
        pinA = pin;
    }

    public void connectB(Pin pin){
        pinB = pin;
    }

    public void connect(Pin pinA, Pin pinB) {
        this.pinA = pinA;
        this.pinB = pinB;
    }

    /**
     * A simple way to attach a watcher in an element, this watch has access to voltage,
     * current, and others parameters, and can modify him.<br>
     * This watcher runs only in the last stap of {@link Circuit#tick(double)} function.
     */
    @SuppressWarnings("unchecked")
    public <T extends Element> void watch(Circuit c, Consumer<T> consumer){
        c.addWatcher(new Watcher<>((T) this, consumer));
    }

    public Pin[] getPins(){
        return new Pin[]{getPinA(), getPinB()};
    }

    /**
     * Returns the value of the element propriety.
     * @return The value that can be edited and will be printed next to the component name in {@link Element#toString()}.<br>
     * Ex: A resistor returns the resistance, a Voltage source returns the voltage.
     */
    public abstract double[] getProperties();
    ///The SI official symbol of the quantity.
    public abstract String[] getPropertiesSymbols();
    /**
     * The current through the element.
     * Using the conventional current direction, so voltage in A is bigger than B, so the positive(+) current direction is from A pin to B pin.
     */
    public abstract double getCurrent();
    public abstract void stamp(MatrixBuilder builder);

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s]: %.2fv, %.2fA, %.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 2),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower()
        );
    }

    /**
     * The voltage difference in the element.
     * Assuming that voltage in A is bigger than B. Therefore, current flows from A to B.
     */
    public double getVoltageDifference(){
        return GET_VOLTAGE_DIFF(getPinA(), getPinB());
    }

    public double getPower() {
        return getVoltageDifference() * getCurrent();
    }

    /**
     * The voltage difference.
     * Assuming that voltage in K is bigger than L. Therefore, current flows from K to L.
     */
    protected static double GET_VOLTAGE_DIFF(Pin k, Pin l){
        double K = 0, L = 0;
        if (k != null)
            if (k.P_voltage == null)
                throw new IllegalStateException("Try get a voltage in a non initialized pin(A)!");
            else
                K = k.P_voltage[0];

        if (l != null)
            if (l.P_voltage == null)
                throw new IllegalStateException("Try get a voltage in a non initialized pin(B)!");
            else
                L = l.P_voltage[0];

        return K-L;
    }

    /**
     * A way to create an element that contains another element as a field.<br>
     */
    public static <T extends Element> T asField(Supplier<T> supplier){
        final T e;
        CircuitBuilder.isFieldElement = true;
        e = supplier.get();
        CircuitBuilder.isFieldElement = false;
        return e;
    }

    @Override
    public void serialize(DataOutputStream stream) throws IOException {
        stream.writeShort(pinA != null ? pinA.address + 1 : 0);
        stream.writeShort(pinB != null ? pinB.address + 1 : 0);
    }

    @Override
    public void unSerialize(DataInputStream buffer) throws IOException {
        short x;
        if ((x = (short) (buffer.readShort()-1)) != -1) {
            this.pinA = new Pin(x);
        } else {
            this.pinA = null;
        }
        if ((x = (short) (buffer.readShort()-1)) != -1) {
            this.pinB = new Pin(x);
        } else {
            this.pinB = null;
        }
    }
}
