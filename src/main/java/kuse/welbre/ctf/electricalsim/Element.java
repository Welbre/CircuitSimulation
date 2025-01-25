package kuse.welbre.ctf.electricalsim;

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

    /**
     * Returns the value of the element propriety.
     * @return The value that can be edited and will be printed next to the component name in {@link Element#toString()}.<br>
     * Ex: A resistor returns the resistance, a Voltage source returns the voltage.
     */
    protected abstract double getPropriety();

    @Override
    public String toString() {
        return String.format(
                "%s(%.2f)[%s,%s]: %.2fv, %.2fA, %.2fW",
                this.getClass().getSimpleName(),
                getPropriety(),
                pinA == null ? "gnd" : pinA.address+1,
                pinB == null ? "gnd" : pinB.address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower()
        );
    }

    public double getVoltageDifference(){
        double a = pinA == null ? 0 : pinA.P_voltage == null ? Double.NaN : pinA.P_voltage[0];
        double b = pinB == null ? 0 : pinB.P_voltage == null ? Double.NaN : pinB.P_voltage[0];
        return b - a;
    }
    public abstract double getCurrent();
    public abstract double getPower();
}
