package kuse.welbre.sim.electricalsim;

public class Capacitor extends Element implements Simulable {
    private double capacitance;
    private double compConductance;
    private double lastVoltage = 0;

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

    @Override
    public double getCurrent() {
        return (getVoltageDifference() - lastVoltage) * compConductance;
    }

    @Override
    public void tick(double dt, Circuit circuit) {
        circuit.getMatrixBuilder().stampCurrentSource(this.getPinA(), this.getPinB(), getVoltageDifference() * compConductance);
        lastVoltage = getVoltageDifference();
    }

    @Override
    public void doInitialTick(Circuit circuit) {
        compConductance = capacitance / Circuit.TIME_STEP;
    }
}
