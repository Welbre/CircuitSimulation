package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.tools.MatrixBuilder;

import java.nio.ByteBuffer;

/**
 * An alternating voltage source (ACVS).<br>
 * The pins A and B are the output pins that the voltage source will be injected,
 * we assume that A > B so, the positive voltage difference is from A To B.<br>
 * The current follow the same idea, the positive direction is from A to B.<br>
 * The {@link ACVoltageSource#frequency frequency} is how many cycles per second the {@link Math#sin(double)} will do.
 */
@SuppressWarnings("unused")
public class ACVoltageSource extends VoltageSource implements Dynamic {
    private static final int CYCLE_RESOLUTION = 2;

    private double frequency;
    //How much angle oscillates per tick.
    private double omega_tick;


    private double outputVoltage;
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

    @Override
    public double getVoltageDifference() {
        return outputVoltage;
    }

    @Override
    public void initiate(Circuit circuit) {
        outputVoltage = 0;
        omega_tick = frequency * 2.0 * Math.PI * circuit.getTickRate();
        theta = omega_tick;
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        outputVoltage = super.getVoltageDifference() * Math.sin(theta);
        builder.stampRHS(getAddress(), outputVoltage);
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

    @Override
    public double getMinTickRate() {
        return 1.0 / (frequency * 4 * CYCLE_RESOLUTION);
    }

    @Override
    public double[] getProperties() {
        return new double[]{super.getVoltageDifference(), frequency};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"V","Hz"};
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        super.serialize(buffer);
        buffer.putDouble(frequency).putDouble(theta);
    }

    @Override
    public void unSerialize(ByteBuffer buffer) {
        super.unSerialize(buffer);
        frequency = buffer.getDouble();
        theta = buffer.getDouble();
    }
}
