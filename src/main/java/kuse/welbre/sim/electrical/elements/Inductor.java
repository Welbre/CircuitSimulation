package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.MatrixBuilder;

/**
 * A linear inductor (L).<br>
 * The pins A and B are where the inductor is connected to.<br>
 * We assume that the voltage in A > voltage in B so, the positive voltage difference is from A to B.<br>
 * Therefore, the positive current direction in from A to B.<br>
 * The {@link Inductor#inductance inductance} is how much voltage per ampere oscillation the inductor can induct.<br<br>
 * <i>The backwards Euler is used to approximate the differential equations of this element.</i>
 */
@SuppressWarnings("unused")
public class Inductor extends Element implements Dynamic {
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
        builder.stampResistor(this.getPinA(), this.getPinB(), compConductance);
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
