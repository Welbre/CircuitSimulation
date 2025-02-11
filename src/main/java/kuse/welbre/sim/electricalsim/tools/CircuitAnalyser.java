package kuse.welbre.sim.electricalsim.tools;

import kuse.welbre.sim.electricalsim.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CircuitAnalyser {
    public final int nodes;
    public final List<VoltageSource> voltageSources;
    public final List<CurrentSource> currentSources;
    public final List<Resistor> resistors;
    public final List<Capacitor> capacitors;
    public final List<Inductor> inductors;
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

        for (Element element : circuit.getElements()) {
            pins.add(element.getPinA());
            pins.add(element.getPinB());
            switch (element) {
                case VoltageSource vs -> voltageSources.add(vs);
                case CurrentSource cs -> currentSources.add(cs);
                case Resistor r -> resistors.add(r);
                case Capacitor cs -> capacitors.add(cs);
                case Inductor i -> inductors.add(i);
                default -> {
                }
            }
        }

        this.nodes = pins.size();

        //removes the ground from the list, because it isn't having relevance in the matrix.
        pins.remove(null);
    }
}
