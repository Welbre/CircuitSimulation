package kuse.welbre.sim.electricalsim.tools;

import kuse.welbre.sim.electricalsim.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CircuitAnalyser {
    public static final class Result {
        public final int nodes;
        public final List<VoltageSource> voltage_source;
        public final List<CurrentSource> current_sources;
        public final List<Resistor> resistors;
        public final List<Capacitor> capacitors;
        public final Set<Element.Pin> pins;

        private Result(Set<Element.Pin> pins, List<VoltageSource> voltage_source, List<CurrentSource> current_sources, List<Resistor> resistors, List<Capacitor> capacitors) {
            //removes the ground from the list, because it isn't having relevance in the matrix.
            pins.remove(null);
            this.nodes = pins.size();
            this.voltage_source = voltage_source;
            this.current_sources = current_sources;
            this.resistors = resistors;
            this.capacitors = capacitors;
            this.pins = pins;
        }
    }

    /**
     * Find all nodes in the circuit.
     * @return A 2-length array that, the 0 addresses is the count of nodes in the circuit, the 1 is the number of independent voltage sources.
     */
    public static CircuitAnalyser.Result analyseCircuit(Circuit circuit) {
        Set<Element.Pin> pins = new HashSet<>();
        List<VoltageSource> voltageSources = new ArrayList<>();
        List<CurrentSource> currentSources = new ArrayList<>();
        List<Resistor> resistors = new ArrayList<>();
        List<Capacitor> capacitors = new ArrayList<>();

        for (Element element : circuit.getElements()) {
            pins.add(element.getPinA());
            pins.add(element.getPinB());
            switch (element) {
                case VoltageSource vs -> voltageSources.add(vs);
                case CurrentSource cs -> currentSources.add(cs);
                case Resistor r -> resistors.add(r);
                case Capacitor cs -> capacitors.add(cs);
                default -> {
                }
            }
        }

        return new Result(pins, voltageSources, currentSources, resistors, capacitors);
    }
}
