package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;

/**
 * A simple diode, the forward current direction is from A to B.<br>
 * This element is governed by <a href="https://en.wikipedia.org/wiki/Shockley_diode_equation">Shockley diode equation</a>,
 * so can't simulate breakdown voltages, and reverse bias.
 */
@SuppressWarnings("unused")
public class Diode extends Element implements NonLinear {
    ///The saturation current, by default 1μA.
    private double saturation = 1e-6;
    ///The quality factor a number bigger than 0.Typically between 0 and 2, but can be higher.
    private double n = 1;
    ///The terminal voltage at 300 K (27ºC) (80ºF)
    private double tempVoltage = 0.025852;
    private double denominator = n * tempVoltage;

    /**
     * Create a default diode model.<br>
     * n = 1; voltageTemperature = 0.025852, leakage = 1e-6.
     */
    public Diode() {
    }

    public Diode(double saturation) {
        setSaturation(saturation);
    }

    public Diode(Pin pinA, Pin pinB, double saturation) {
        super(pinA, pinB);
        setSaturation(saturation);
    }

    public Diode(double n, double saturation) {
        this.n = n;
        this.saturation = saturation;
        denominator = n * tempVoltage;
    }

    /**
     * Create diode modulating n to pass in point (forwardVoltage, openCurrent) at I-V plane.
     * @param forwardVoltage the voltage that the openCurrent will flow.
     * @param openCurrent the current that will flow in forwardVoltage.
     * @param saturation the saturation current.
     */
    public Diode(double forwardVoltage, double openCurrent, double saturation){
        this(forwardVoltage / (0.025852 * Math.log((openCurrent / saturation) + 1)), saturation);
    }

    public Diode(Pin pinA, Pin pinB, double n, double saturation) {
        super(pinA, pinB);
        this.n = n;
        this.saturation = saturation;
        denominator = n * tempVoltage;
    }

    ///Shockley diode equation.
    ///@see <a href="https://en.wikipedia.org/wiki/Shockley_diode_equation">Shockley diode equation</a>
    @Override
    public double plane_I_V(double voltage) {
        return saturation * (Math.exp(voltage/denominator) - 1);
    }

    ///The derivative of Shockley diode equation in respect of voltage.
    public double plane_dI_dV(double voltage) {
        return (saturation / denominator) * Math.exp(voltage/denominator);
    }

    @Override
    public void smallSignalStamp(MatrixBuilder builder) {
        builder.stampResistor(getPinA(), getPinB(), plane_dI_dV(getVoltageDifference()));
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampCurrentSource(getPinA(), getPinB(), -plane_I_V(getVoltageDifference()));
    }

    ///Returns the threshold voltage of the diode, using 1mA as reference to "on" state.
    @Override
    public double getQuantity() {
        return denominator * Math.log(1e-3/(saturation) + 1);
    }

    @Override
    public String getQuantitySymbol() {
        return "V";
    }

    @Override
    public double getCurrent() {
        return plane_I_V(getVoltageDifference());
    }

    public void setSaturation(double saturation) {
        this.saturation = saturation;
    }

    public void setN(double n) {
        if (n > 0)
            throw new IllegalArgumentException("The quality factor need to be bigger that 0");
        this.n = n;
        denominator = n * tempVoltage;
    }

    public void setTempVoltage(double tempVoltage) {
        this.tempVoltage = tempVoltage;
        denominator = n * tempVoltage;
    }

    public double getSaturation() {
        return saturation;
    }

    public double getN() {
        return n;
    }
}
