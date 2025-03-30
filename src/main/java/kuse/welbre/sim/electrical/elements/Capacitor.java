package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.MatrixBuilder;

/**
 * A linear capacitor (C).<br>
 * The pins A and B are where the capacitor is connected to.<br>
 * We assume that the voltage in A > voltage in B so, the positive voltage difference is from A to B.<br>
 * Therefore, the positive current direction in from A to B.<br>
 * The {@link Capacitor#capacitance capacitance} is how much charge per volt the capacitor can hold.<br<br>
 * <i>The backwards Euler is used to approximate the differential equations of this element.</i>
 */
@SuppressWarnings("unused")
public class Capacitor extends Element implements Dynamic {
    private double capacitance;
    public double compConductance;
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
    public double[] getProperties() {
        return new double[]{capacitance};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"F"};
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampConductance(this.getPinA(), this.getPinB(), compConductance);
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
        currentSource = compConductance * getVoltageDifference();//voltage at t
        builder.stampCurrentSource(getPinA(), getPinB(), currentSource);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        capacitorCurrent = +compConductance * getVoltageDifference() - currentSource;//voltage at t+1
    }
}
