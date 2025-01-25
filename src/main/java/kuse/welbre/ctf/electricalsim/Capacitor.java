package kuse.welbre.ctf.electricalsim;

public class Capacitor extends CurrentSource implements Simulable {
    private double capacitance;
    private double dv_dt = 0;
    private double charge = 0;

    public Capacitor() {
        capacitance = 0;
    }

    public Capacitor(double capacitance) {
        this.capacitance = capacitance;
    }

    public Capacitor(Pin pinA, Pin pinB, double capacitance) {
        super(pinA, pinB, 0);
        this.capacitance = capacitance;
    }

    @Override
    protected double getPropriety() {
        return capacitance;
    }

    @Override
    public double getCurrent() {
        return capacitance * dv_dt;
    }

    @Override
    public void tick(double dt) {
        dv_dt = (getVoltageDifference() - dv_dt) / dt;
    }


    public double getCapacitance() {
        return capacitance;
    }

    public void setCapacitance(double capacitance) {
        this.capacitance = capacitance;
        this.charge = 0;
    }

    public double getCharge() {
        return charge;
    }
}
