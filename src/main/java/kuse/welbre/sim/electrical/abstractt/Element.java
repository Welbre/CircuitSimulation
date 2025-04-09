package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.Circuit.Pin;
import kuse.welbre.sim.electrical.CircuitBuilder;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Element implements Serializable {
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

    public void connect(Pin pin, int idx){
        if (idx == 0)
            pinA = pin;
        else if (idx == 1) {
            pinB = pin;
        }
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
        return Pin.GET_VOLTAGE_DIFF(getPinA(), getPinB());
    }

    public double getPower() {
        return getVoltageDifference() * getCurrent();
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
        this.connectA((x = (short) (buffer.readShort()-1)) != -1 ? new Pin(x) : null);
        this.connectB((x = (short) (buffer.readShort()-1)) != -1 ? new Pin(x) : null);
    }
}
