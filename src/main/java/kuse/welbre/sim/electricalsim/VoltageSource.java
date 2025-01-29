package kuse.welbre.sim.electricalsim;

public class VoltageSource extends Element {
    private double voltage;
    private double[] current;

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
        return current == null ? Double.NaN : current[0];
    }

    @Override
    protected double getPropriety() {
        return voltage;
    }

    public void setCurrentPointer(double[] pointer) {
        current = pointer;
    }
}
