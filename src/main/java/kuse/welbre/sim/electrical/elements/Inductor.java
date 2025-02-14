package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Simulable;
import kuse.welbre.tools.MatrixBuilder;

@SuppressWarnings("unused")
public class Inductor extends Element implements Simulable {
    private double inductance;
    private double compConductance;
    private double currentSource;

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
    public double getQuantity() {
        return inductance;
    }

    @Override
    public String getQuantitySymbol() {
        return "H";
    }

    public double getInductance() {
        return inductance;
    }

    @Override
    public double getCurrent() {
        return currentSource;
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampInductor(this.getPinA(), this.getPinB(), compConductance, this.getCurrent());
    }

    @Override
    public void initiate(Circuit circuit) {
        compConductance = circuit.getTickRate() / getInductance();
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        builder.stampCurrentSource(getPinB(), getPinA(), currentSource);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        currentSource = currentSource + (compConductance * getVoltageDifference());
    }
}
