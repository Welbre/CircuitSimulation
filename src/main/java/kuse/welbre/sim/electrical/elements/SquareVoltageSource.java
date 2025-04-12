package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.RHSElement;
import kuse.welbre.tools.MatrixBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
public class SquareVoltageSource extends Element implements Dynamic, RHSElement {
    private int idx;
    private double[] current;

    private double outputVoltage;


    private double voltage;
    private double frequency;
    private double period;
    private double dutyCycle;
    private double phaseShift;//Phase in radians.
    private double v_off;

    private double tickRate;
    private double time;

    public SquareVoltageSource(double voltage, double frequency, double dutyCycle, double phaseShift) {
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle / frequency;
        this.phaseShift = phaseShift;
        this.time = period * phaseShift / (2 * Math.PI);//shift the time instead of shift the voltage calculation.;
    }

    public SquareVoltageSource(Circuit.Pin pinA, Circuit.Pin pinB, double voltage, double frequency, double dutyCycle, double phaseShift) {
        super(pinA, pinB);
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle / frequency;
        this.phaseShift = phaseShift;
        this.time = period * phaseShift / (2 * Math.PI);//shift the time instead of shift the voltage calculation.;
    }

    public SquareVoltageSource(double voltage, double frequency, double dutyCycle) {
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle / frequency;
    }

    public SquareVoltageSource(Circuit.Pin pinA, Circuit.Pin pinB, double voltage, double frequency, double dutyCycle) {
        super(pinA, pinB);
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle / frequency;
    }

    public SquareVoltageSource(double voltage, double frequency) {
        this.voltage = voltage;
        this.frequency = frequency;
    }

    public SquareVoltageSource(Circuit.Pin pinA, Circuit.Pin pinB, double voltage, double frequency) {
        super(pinA, pinB);
        this.voltage = voltage;
        this.frequency = frequency;
    }

    public SquareVoltageSource() {
    }

    public SquareVoltageSource(Circuit.Pin pinA, Circuit.Pin pinB) {
        super(pinA, pinB);
    }

    public SquareVoltageSource(double voltage) {
        this.voltage = voltage;
    }

    public SquareVoltageSource(Circuit.Pin pinA, Circuit.Pin pinB, double voltage) {
        super(pinA, pinB);
        this.voltage = voltage;
    }

    @Override
    public void initiate(Circuit circuit) {
        period = 1.0 / frequency;
        tickRate = circuit.getTickRate();
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        outputVoltage = ((time % period) < dutyCycle ? voltage : -voltage) + v_off;
        builder.stampRHS(idx, outputVoltage);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        time += tickRate;
    }

    @Override
    public double[] getProperties() {
        return new double[]{voltage+v_off,-voltage+v_off, frequency, dutyCycle*frequency*100};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"V", "V", "Hz", "%"};
    }

    @Override
    public double getCurrent() {
        if (current == null)
            return Double.NaN;
        return current[0];
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampVoltageSource(getPinA(), getPinB(), idx);
    }

    @Override
    public void setValuePointer(double[] pointer) {
        current = pointer;
    }

    @Override
    public int getAddress() {
        return idx;
    }

    @Override
    public void setAddress(short address) {
        idx = address;
    }

    @Override
    public double getVoltageDifference() {
        return outputVoltage;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
        this.period = 1.0/frequency;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getPhaseShift() {
        return phaseShift;
    }

    /**
     * Set the phase shift of the wave
     * @param phaseShift The amount of shift in radians. Should be a value between 0 and 2*PI.
     */
    public void setPhaseShift(double phaseShift) {
        this.phaseShift = phaseShift;
        this.time += period * phaseShift / (2 * Math.PI);//shift the time instead of shift the voltage calculation.;
    }

    public double getV_off() {
        return v_off;
    }

    public void setV_off(double v_off) {
        this.v_off = v_off;
    }

    public double getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle /2.0;
    }

    @Override
    public void serialize(DataOutputStream s) throws IOException {
        super.serialize(s);
        s.writeDouble(voltage);
        s.writeDouble(frequency);
        s.writeDouble(period);
        s.writeDouble(dutyCycle);
        s.writeDouble(phaseShift);
        s.writeDouble(v_off);
        s.writeDouble(time);
        s.writeDouble(outputVoltage);
    }

    @Override
    public void unSerialize(DataInputStream s) throws IOException  {
        super.unSerialize(s);
        voltage = s.readDouble();
        frequency = s.readDouble();
        period = s.readDouble();
        dutyCycle = s.readDouble();
        phaseShift = s.readDouble();
        v_off = s.readDouble();
        time = s.readDouble();
        outputVoltage = s.readDouble();
    }
}
