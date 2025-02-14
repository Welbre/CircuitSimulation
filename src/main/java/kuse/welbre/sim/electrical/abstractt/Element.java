package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.util.Random;

public abstract class Element {
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
    }
    private Pin pinA;
    private Pin pinB;

    public Element() {
        pinA = new Pin();
        pinB = new Pin();
    }

    public Element(Pin pinA, Pin pinB) {
        this.pinA = pinA;
        this.pinB = pinB;
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

    public Pin[] getPins(){
        return new Pin[]{getPinA(), getPinB()};
    }

    /**
     * Returns the value of the element propriety.
     * @return The value that can be edited and will be printed next to the component name in {@link Element#toString()}.<br>
     * Ex: A resistor returns the resistance, a Voltage source returns the voltage.
     */
    public abstract double getQuantity();
    ///The SI official symbol of the quantity.
    public abstract String getQuantitySymbol();
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
                Tools.proprietyToSi(getQuantity(), getQuantitySymbol(), 2),
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
    public static double GET_VOLTAGE_DIFF(Pin k, Pin l){
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
}
