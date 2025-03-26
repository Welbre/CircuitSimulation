package kuse.welbre.sim;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import kuse.welbre.tools.LU;

public class CircuitBenchmark {
    private static final int size = 800;
    private Circuit circuit;

    @Benchmark.preBenchmark
    @Benchmark.Order(1)
    void model(){
        circuit = new Circuit();
        VoltageSource v = new VoltageSource(800);
        circuit.addElement(v);

        v.connectB(null);
        Element.Pin last = v.getPinA();

        Resistor r = null;

        for (int i = 0; i < size; i++) {
            r = new Resistor(1);
            circuit.addElement(r);
            r.connectA(last);
            last = r.getPinB();
        }
        r.connectB(null);
    }

    @Benchmark.preBenchmark
    @Benchmark.Order(2)
    void preCompile(){
        circuit.preCompile();
    }

    @Benchmark.benchmark
    void mark(){
        circuit.tick(1);
    }

    public static void main(String[] args) {
        new Benchmark(CircuitBenchmark.class).benchmark(500);
    }
}
