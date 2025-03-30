package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.RHSElement;
import kuse.welbre.tools.MatrixBuilder;

@SuppressWarnings("unused")
public class SquareVoltageSource extends Element implements Dynamic, RHSElement {
    private int idx;
    private double current[];

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
        this.dutyCycle = dutyCycle;
        this.phaseShift = phaseShift;
        this.time = period * phaseShift / (2 * Math.PI);//shift the time instead of shift the voltage calculation.;
    }

    public SquareVoltageSource(Pin pinA, Pin pinB, double voltage, double frequency, double dutyCycle, double phaseShift) {
        super(pinA, pinB);
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
        this.phaseShift = phaseShift;
        this.time = period * phaseShift / (2 * Math.PI);//shift the time instead of shift the voltage calculation.;
    }

    public SquareVoltageSource(double voltage, double frequency, double dutyCycle) {
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
    }

    public SquareVoltageSource(Pin pinA, Pin pinB, double voltage, double frequency, double dutyCycle) {
        super(pinA, pinB);
        this.voltage = voltage;
        this.frequency = frequency;
        this.dutyCycle = dutyCycle;
    }

    public SquareVoltageSource(double voltage, double frequency) {
        this.voltage = voltage;
        this.frequency = frequency;
    }

    public SquareVoltageSource(Pin pinA, Pin pinB, double voltage, double frequency) {
        super(pinA, pinB);
        this.voltage = voltage;
        this.frequency = frequency;
    }

    public SquareVoltageSource() {
    }

    public SquareVoltageSource(Pin pinA, Pin pinB) {
        super(pinA, pinB);
    }

    public SquareVoltageSource(double voltage) {
        this.voltage = voltage;
    }

    public SquareVoltageSource(Pin pinA, Pin pinB, double voltage) {
        super(pinA, pinB);
        this.voltage = voltage;
    }

    @Override
    public void initiate(Circuit circuit) {
        period = 1.0 / frequency;
        outputVoltage = ((time % period) < dutyCycle ? voltage : -voltage) + v_off;
        tickRate = circuit.getTickRate();
        System.out.println("stated");
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        outputVoltage = ((time % period) < dutyCycle ? voltage : -voltage) + v_off;
        builder.stampRHS(idx, outputVoltage);
        System.out.println("updated %f time %f".formatted(outputVoltage, time));
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        time += tickRate;
    }

    @Override
    public double getQuantity() {
        return voltage;
    }

    @Override
    public String getQuantitySymbol() {
        return "V";
    }

    @Override
    public double getCurrent() {
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
        this.dutyCycle = dutyCycle;
    }
}
