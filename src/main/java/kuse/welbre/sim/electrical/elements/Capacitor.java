package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.MatrixBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

    private double capacitorCurrent = 0;
    private double vDif = 0;

    public Capacitor() {
    }

    public Capacitor(double capacitance) {
        this.capacitance = capacitance;
    }

    public Capacitor(Circuit.Pin pinA, Circuit.Pin pinB, double capacitance) {
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

    @Override
    public double getVoltageDifference() {
        return vDif;
    }

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
        currentSource = compConductance * vDif;//voltage at t
        builder.stampCurrentSource(getPinA(), getPinB(), currentSource);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        vDif = super.getVoltageDifference();
        capacitorCurrent = compConductance * vDif - currentSource;//voltage at t+1
    }

    @Override
    public void serialize(DataOutputStream s) throws IOException {
        super.serialize(s);
        s.writeDouble(capacitance);
        s.writeDouble(currentSource);//todo this is useless, write the voltage at this point, instead the currentSource.
        s.writeDouble(vDif);//todo this is useless, write the voltage at this point, instead the currentSource.
        s.writeDouble(capacitorCurrent);
    }

    @Override
    public void unSerialize(DataInputStream s) throws IOException {
        super.unSerialize(s);
        capacitance = s.readDouble();
        currentSource = s.readDouble();
        vDif = s.readDouble();
        capacitorCurrent = s.readDouble();

        //todo nesse ponto, quando o elemento é desSerializado ele perde o valor de currentSource pq o circuito irar executar uma simulação com um tickRate minimo, para calcular o ponto de operação Dc
        //todo para consertar isso, preciso fazer uma simulação dc, ao invés de usar um timestep pequeno, assim vou conseguir por um valor inicial de tensão para os capacitores e indutores e outros elementos dinâmicos
    }
}
