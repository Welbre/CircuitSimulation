package kuse.welbre.sim;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.sim.electrical.elements.VoltageSource;

public class CircuitBenchmark {
    private static final int size = 80;
    private Circuit circuit;

    @Benchmark.preBenchmark
    @Benchmark.Order(1)
    void pre(){
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
    void compile(){
        circuit.preCompile();
    }

    @Benchmark.benchmark
    void mark(){
        for (int i = 0; i < (int) Math.floor(1.0 / circuit.getTickRate() + 0.5); i++)
            circuit.tick(0);
    }

    public static void main(String[] args) {
        new Benchmark(CircuitBenchmark.class).benchmark();
    }
}
