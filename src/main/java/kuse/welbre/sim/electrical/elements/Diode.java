package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;

public class Diode extends Element implements NonLinear {
    ///The saturation current, by default 1pA.
    private double saturation = 1e-12;
    ///The quality factor a number bigger than 0.Typically between 0 and 2, but can be higher.
    private double n = 1;
    ///The terminal voltage at 300 K and (27ºC)
    private double tempVoltage = 0.025852;
    private double denominator = n * tempVoltage;

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

    public Diode(Pin pinA, Pin pinB, double n, double saturation) {
        super(pinA, pinB);
        this.n = n;
        this.saturation = saturation;
        denominator = n * tempVoltage;
    }

    ///Shockley diode equation.
    ///@see <a href="https://en.wikipedia.org/wiki/Shockley_diode_equation">Shockley diode equation</a>
    @Override
    public double I_V_Plane(double voltage) {
        return saturation * (Math.exp(voltage/denominator) - 1);
    }

    ///The derivative of Shockley diode equation in respect of voltage.
    @Override
    public double dI_dV_Plane(double voltage) {
        return (saturation / denominator) * Math.exp(voltage/denominator);
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampResistor(getPinA(), getPinB(), dI_dV_Plane(getVoltageDifference()));
        builder.stampCurrentSource(getPinA(), getPinB(), -getCurrent());
    }

    ///Returns the threshold voltage of the diode, using 1mA as reference to "on" state.
    @Override
    public double getQuantity() {
        return denominator * Math.log(1e-3/saturation);
    }

    @Override
    public String getQuantitySymbol() {
        return "V";
    }

    @Override
    public double getCurrent() {
        return I_V_Plane(getVoltageDifference());
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
