package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.abstractt.Simulable;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

public class ACVoltageSource extends VoltageSource implements Simulable {
    private double frequency;
    //How much angle oscillates per tick.
    private double omega_tick;
    //The angles of sin
    private double theta;

    public ACVoltageSource() {
    }

    public ACVoltageSource(double voltage, double frequency) {
        super(voltage);
        this.frequency = frequency;
    }

    public ACVoltageSource(Pin nodeA, Pin nodeB, double voltage, double frequency) {
        super(nodeA, nodeB, voltage);
        this.frequency = frequency;
    }

    private double outputVoltage;
    @Override
    public double getVoltageDifference() {
        return outputVoltage;
    }

    @Override
    public void initiate(Circuit circuit) {
        outputVoltage = 0;
        omega_tick = frequency * 2.0 * Math.PI * circuit.getTickRate();
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        outputVoltage = super.getVoltageDifference() * Math.sin(theta);
        builder.stampZMatrixVoltageSource(getAddress(), outputVoltage);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        theta = theta + omega_tick;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }
}
