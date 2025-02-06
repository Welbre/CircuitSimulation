package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

public class Inductor extends Element implements Simulable {
    private double inductance;
    private double compResistor;
    private double lastCurrent;

    public Inductor() {
    }

    public Inductor(double inductance) {
        this.inductance = inductance;
    }

    public Inductor(Pin pinA, Pin pinB, double inductance) {
        super(pinA, pinB);
        this.inductance = inductance;
    }

    @Override
    protected double getPropriety() {
        return inductance;
    }

    @Override
    protected String getProprietySymbol() {
        return "H";
    }

    public double getInductance() {
        return inductance;
    }

    @Override
    public double getCurrent() {
        return 0;
    }

    @Override
    public void tick(double dt, MatrixBuilder builder) {
        builder.stampCurrentSource(getPinA(), getPinB(), lastCurrent);
    }

    @Override
    public void posTick(){
        this.lastCurrent = (-getVoltageDifference() / compResistor) + lastCurrent;
    }

    @Override
    public void doInitialTick(MatrixBuilder builder) {
        compResistor = inductance / Circuit.TIME_STEP;
    }
}
