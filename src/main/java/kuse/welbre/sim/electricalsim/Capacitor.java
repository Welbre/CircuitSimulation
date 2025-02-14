package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.abstractt.Element;
import kuse.welbre.sim.electricalsim.abstractt.Simulable;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

@SuppressWarnings("unused")
public class Capacitor extends Element implements Simulable {
    private double capacitance;
    private double compConductance;
    private double currentSource;

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
    protected double getQuantity() {
        return capacitance;
    }

    @Override
    protected String getQuantitySymbol() {
        return "F";
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampCapacitor(this.getPinA(), this.getPinB(), compConductance, this.getCurrent());
    }

    private double capacitorCurrent = 0;
    @Override
    public double getCurrent() {
        return capacitorCurrent;
    }

    @Override
    public void initiate(Circuit circuit) {
        compConductance = getCapacitance() / circuit.getTickRate();
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        currentSource = compConductance * getVoltageDifference();
        builder.stampCurrentSource(getPinA(), getPinB(), currentSource);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        capacitorCurrent = compConductance * getVoltageDifference() - currentSource;
    }
}
