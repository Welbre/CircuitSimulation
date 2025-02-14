package kuse.welbre.sim.electricalsim.tools;

import kuse.welbre.sim.electricalsim.*;
import kuse.welbre.sim.electricalsim.abstractt.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CircuitAnalyser {
    public final int nodes;
    public final int matrixSize;
    public final List<VoltageSource> voltageSources;
    public final List<CurrentSource> currentSources;
    public final List<Resistor> resistors;
    public final List<Capacitor> capacitors;
    public final List<Inductor> inductors;
    public final List<CCCS> cccs;
    public final List<VCVS> vcvs;
    public final Set<Element.Pin> pins;

    /**
     * Find all nodes in the circuit.
     * A 2-length array that, the 0 addresses is the count of nodes in the circuit, the 1 is the number of independent voltage sources.
     */
    public CircuitAnalyser(Circuit circuit) {
        pins = new HashSet<>();
        voltageSources = new ArrayList<>();
        currentSources = new ArrayList<>();
        resistors = new ArrayList<>();
        capacitors = new ArrayList<>();
        inductors = new ArrayList<>();
        cccs = new ArrayList<>();
        vcvs = new ArrayList<>();

        for (Element element : circuit.getElements()) {
            pins.add(element.getPinA());
            pins.add(element.getPinB());
            switch (element) {
                case VoltageSource vs -> voltageSources.add(vs);
                case CurrentSource cs -> currentSources.add(cs);
                case Resistor r -> resistors.add(r);
                case Capacitor cs -> capacitors.add(cs);
                case Inductor i -> inductors.add(i);
                case CCCS cs -> cccs.add(cs);//Current-controlled current source.
                case VCVS vs -> vcvs.add(vs);//Voltage_controlled voltage source.
                default -> {
                }
            }
        }

        //removes the ground from the list, because it isn't having relevance in the matrix.
        pins.remove(null);

        this.nodes = pins.size();
        //Each of these terms contributes to matrix size
        //The node with an unknown voltage, and voltage sources with unknown currents.
        this.matrixSize = this.nodes + voltageSources.size() + cccs.size() + vcvs.size();
    }
}
