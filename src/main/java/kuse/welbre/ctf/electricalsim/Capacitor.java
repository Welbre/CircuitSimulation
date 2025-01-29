package kuse.welbre.ctf.electricalsim;

public class Capacitor extends Element implements Simulable {
    private double capacitance;
    private double compConductance;

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
    public double getCurrent() {
        return getVoltageDifference() * compConductance;
    }

    @Override
    public void tick(double dt, Circuit circuit) {
        circuit.getMatrixBuilder().stampCurrentSource(this.getPinA(), this.getPinB(), getCurrent());
    }

    @Override
    public void doInitialTick(Circuit circuit) {
        compConductance = capacitance / Circuit.TIME_STEP;
    }
}
