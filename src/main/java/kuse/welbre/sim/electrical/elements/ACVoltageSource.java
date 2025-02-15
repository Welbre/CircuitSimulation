package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Simulable;
import kuse.welbre.tools.MatrixBuilder;

/**
 * An alternating voltage source (ACVS).<br>
 * The pins A and B are the output pins that the voltage source will be injected,
 * we assume that A > B so, the positive voltage difference is from A To B.<br>
 * The current follow the same idea, the positive direction is from A to B.<br>
 * The {@link ACVoltageSource#frequency frequency} is how many cycles per second the {@link Math#sin(double)} will do.
 */
@SuppressWarnings("unused")
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
