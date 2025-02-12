package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

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
    public void stamp(MatrixBuilder builder) {
        builder.stampResistor(this.getPinA(), this.getPinB(), this.getConductance());
    }

    @Override
    protected double getPropriety() {
        return resistance;
    }

    @Override
    protected String getProprietySymbol() {
        return "Ω";
    }
}
