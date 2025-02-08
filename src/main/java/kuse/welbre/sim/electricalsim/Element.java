package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.Tools;

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
    protected abstract String getProprietySymbol();
    /**
     * The current through the element.
     * Using the conventional current direction, so Voltage in A is bigger than B, so the positive(+) current direction is from A pin to B pin.
     */
    public abstract double getCurrent();

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s]: %.2fv, %.2fA, %.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getPropriety(), getProprietySymbol(), 2),
                pinA == null ? "gnd" : pinA.address+1,
                pinB == null ? "gnd" : pinB.address+1,
                getVoltageDifference(),
                getCurrent(),
                getPower()
        );
    }

    /**
     * The voltage difference in the element.
     * Assuming that Voltage in A is bigger than B.
     */
    public double getVoltageDifference(){
        double a = 0, b = 0;
        if (pinA != null)
            if (pinA.P_voltage == null)
                throw new IllegalStateException("Try get a voltage in a non initialized pin(A)!");
            else
                a = pinA.P_voltage[0];

        if (pinB != null)
            if (pinB.P_voltage == null)
                throw new IllegalStateException("Try get a voltage in a non initialized pin(B)!");
            else
                b = pinB.P_voltage[0];

        return a-b;
    }
    public double getPower() {
        return getVoltageDifference() * getCurrent();
    }
}
