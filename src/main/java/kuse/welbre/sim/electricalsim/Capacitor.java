package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

public class Capacitor extends Element implements Simulable {
    private double capacitance;
    private double compConductance;
    private double currentSource = 0;

    public Capacitor() {
    }

    public Capacitor(double capacitance) {
        this.capacitance = capacitance;
    }

    public Capacitor(Pin pinA, Pin pinB, double capacitance) {
        super(pinA, pinB);
        this.capacitance = capacitance;
    }

    public double getCapacitance() {
        return capacitance;
    }

    @Override
    protected double getPropriety() {
        return capacitance;
    }

    @Override
    protected String getProprietySymbol() {
        return "F";
    }

    double lastVoltageDif;
    @Override
    public double getCurrent() {
        return compConductance * (lastVoltageDif - getVoltageDifference());
    }

    @Override
    public void initiate(Circuit circuit) {
        compConductance = getCapacitance() / circuit.getTickRate();
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        builder.stampCurrentSource(getPinA(), getPinB(), currentSource);
        lastVoltageDif = getVoltageDifference();
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        currentSource = compConductance * getVoltageDifference();
    }
}
