package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.CircuitAnalyser;

import java.util.Random;

public class VoltageSource extends Element {
    private double voltage;
    private double[] current;
    /// This field represents the row of the Z matrix that then voltage source will set the voltage.
    /// Starts will a random number, but will be assigned in {@link Circuit#preparePinsAndSources(CircuitAnalyser.Result, double[][]) preparePinsAndSources};
    public short address = (short) new Random().nextInt();

    public VoltageSource() {
    }

    public VoltageSource(double voltage) {
        this.voltage = voltage;
    }

    public VoltageSource(Pin nodeA, Pin nodeB, double voltage) {
        super(nodeA, nodeB);
        this.voltage = voltage;
    }

    @Override
    public double getVoltageDifference() {
        return voltage;
    }

    @Override
    public double getCurrent() {
        if (current == null)
            throw new IllegalStateException("Try to get a current in a voltage source that doesn't be initiated!");
        return current[0];
    }

    @Override
    protected double getPropriety() {
        return voltage;
    }

    @Override
    protected String getProprietySymbol() {
        return "V";
    }

    public void setCurrentPointer(double[] pointer) {
        current = pointer;
    }
}
