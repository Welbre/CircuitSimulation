package kuse.welbre.sim.electricalsim;

public class Resistor extends Element {
    private double resistance;
    private double conductance;

    public Resistor() {
        resistance = Double.MAX_VALUE;
        conductance = Double.MIN_VALUE;
    }

    public Resistor(double resistance) {
        setResistance(resistance);
    }

    public Resistor(Pin nodeA, Pin nodeB, double resistance) {
        super(nodeA, nodeB);
        setResistance(resistance);
    }

    public void setResistance(double r){
        resistance = r;
        conductance = 1.0 / r;
    }

    public double getResistance() {
        return resistance;
    }

    public double getConductance() {
        return conductance;
    }

    @Override
    public double getCurrent() {
        return getVoltageDifference() / getResistance();
    }

    @Override
    protected double getPropriety() {
        return resistance;
    }
}
