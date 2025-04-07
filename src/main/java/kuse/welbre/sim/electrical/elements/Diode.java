package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;

import java.nio.ByteBuffer;

/**
 * A simple diode, the forward current direction is from A to B.<br>
 * This element is governed by <a href="https://en.wikipedia.org/wiki/Shockley_diode_equation">Shockley diode equation</a>,
 * so can't simulate breakdown voltages, and reverse bias.
 */
@SuppressWarnings("unused")
public class Diode extends Element implements NonLinear {
    public static final double DEFAULT_SATURATION = 1e-6;
    ///The saturation current, by default 1μA.
    private double saturation = DEFAULT_SATURATION;
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

    public Diode(Pin pinA, Pin pinB) {
        super(pinA, pinB);
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

    @Override
    public void stamp_I_V(MatrixBuilder builder) {
        builder.stampCurrentSource(getPinA(), getPinB(), getCurrent());
    }

    @Override
    public void stamp_dI_dV(MatrixBuilder builder) {
        builder.stampConductance(getPinA(), getPinB(), Math.max(conductance(),1e-12));
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampCurrentSource(getPinA(), getPinB(), getCurrent());
        builder.stampConductance(getPinA(), getPinB(), conductance());
    }

    ///Returns the threshold voltage of the diode, using 1mA as reference to "on" state.
    @Override
    public double[] getProperties() {
        return new double[]{denominator * Math.log(1e-3 / (saturation) + 1)};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"V"};
    }

    ///Shockley diode equation.
    ///@see <a href="https://en.wikipedia.org/wiki/Shockley_diode_equation">Shockley diode equation</a>
    @Override
    public double getCurrent() {
        return -(saturation * (Math.exp(getVoltageDifference()/denominator) - 1));
    }

    ///The derivative of Shockley diode equation in respect of voltage.
    private double conductance() {
        return (saturation / denominator) * Math.exp(getVoltageDifference()/denominator);
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

    @Override
    public void serialize(ByteBuffer buffer) {
        super.serialize(buffer);
        buffer.putDouble(saturation).putDouble(n).putDouble(tempVoltage);
    }

    @Override
    public void unSerialize(ByteBuffer buffer) {
        super.unSerialize(buffer);
        saturation = buffer.getDouble();
        n = buffer.getDouble();
        tempVoltage = buffer.getDouble();
        denominator = n * tempVoltage;
    }
}
