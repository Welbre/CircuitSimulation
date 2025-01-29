package kuse.welbre.ctf.electricalsim;

import kuse.welbre.ctf.electricalsim.Element.Pin;
import kuse.welbre.ctf.electricalsim.tools.Tools;

import java.util.*;

public class Circuit {
    /** 5ms time step */
    public static final double TIME_STEP = 0.005;

    private final List<Element> elements = new ArrayList<>();
    private final List<Simulable> simulableElements = new ArrayList<>();

    private double[][] inverse = null;
    /**
     * Hold the known values.
     */
    private double[] Z = null;
    /**
     * This array storage 'n' nodes voltage pointers and 'm' current throw a voltage source.<br>
     * The array it's a double[n+m][1], each index represents a double pointer, so this allows passing a reference for each element
     * and modify only once.
     */
    private double[][] X = null;
    private double[][] G = null;

    private boolean isDirt = true;

    @SafeVarargs
    public final <T extends Element> void addElement(T... elements) {
        for (T element : elements)
            if (!this.elements.contains(element)) {
                this.elements.add(element);
                if (element instanceof Simulable sim)
                    simulableElements.add(sim);
            }
    }

    /**
     * Try to find if some node or element can crash the matrix generation.
     */
    private void checkInconsistencies() {
        //todo need's to be implemented.
    }

    private static final class AnalysesResult {
        public final int nodes;
        public final List<VoltageSource> voltage_source;
        public final List<CurrentSource> current_sources;
        public final List<Resistor> resistors;
        public final List<Capacitor> capacitors;
        public final Set<Pin> pins;

        public AnalysesResult(Set<Pin> pins, List<VoltageSource> voltage_source, List<CurrentSource> current_sources, List<Resistor> resistors, List<Capacitor> capacitors) {
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
    private AnalysesResult analyseNodesAndElements() {
        Set<Pin> pins = new HashSet<>();
        List<VoltageSource> voltageSources = new ArrayList<>();
        List<CurrentSource> currentSources = new ArrayList<>();
        List<Resistor> resistors = new ArrayList<>();
        List<Capacitor> capacitors = new ArrayList<>();

        for (Element element : elements) {
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

        return new AnalysesResult(pins, voltageSources, currentSources, resistors, capacitors);
    }

    /**
     * Convert the random pins address to a workable value, the address is some short number between 0 and {@link Short#MAX_VALUE}.<br>
     * Set the pointers in the {@link Pin pin}, and set the voltageSource current pointer.
     */
    private void preparePinsAndSources(AnalysesResult result){
        short next = 0;
        for (Pin pin : result.pins) {
            //Set to useful index in a matrix.
            pin.address = next;
            //Set the pointer address to the same in X matrix.
            pin.P_voltage = X[next++];
        }

        for (int i = 0; i < result.voltage_source.size(); i++) {
            //Set the current pointer.
            result.voltage_source.get(i).setCurrentPointer(X[i + result.pins.size()]);
        }
    }

    /**
     * An aid to build the G matrix, uses null to connect the element to ground.
     */
    public static final class MatrixBuilder {
        private final double[][] G;
        private final double[] Z;

        public MatrixBuilder(double[][] g, double[] z) {
            G = g;
            Z = z;
        }

        public void stampResistor(Pin a, Pin b, double conductance) {
            if (a != null) {
                G[a.address][a.address] += conductance;
                if (b != null) {
                    G[a.address][b.address] -= conductance;
                    G[b.address][a.address] -= conductance;
                }
            }
            if (b != null) {
                G[b.address][b.address] += conductance;
            }
        }

        public void stampVoltageSource(Pin a, Pin b, int nodes_length, double voltage){
            if (a != null) {
                G[a.address][nodes_length] = 1;
                G[nodes_length][a.address] = 1;
            }
            if (b != null) {
                G[b.address][nodes_length] = -1;
                G[nodes_length][b.address] = -1;
            }
            Z[nodes_length] = voltage;
        }

        public void stampCurrentSource(Pin a, Pin b, double current){
            //todo the current need's to add from a and subtracted from b, this is only a test to implement capacitors.
            if (a != null)
                Z[a.address] += current;
            if (b != null)
                Z[b.address] -= current;
        }

        public void stampCapacitor(Pin a, Pin b, double capacitance_per_tick, double current){
            stampResistor(a, b, capacitance_per_tick);
            stampCurrentSource(a, b, current);
        }
    }

    public void buildMatrix() {
        final AnalysesResult result = analyseNodesAndElements();
        final int nm = result.nodes + result.voltage_source.size();
        final double[][] G = new double[nm][nm];
        final double[] Z = new double[nm];
        X = new double[nm][1];
        preparePinsAndSources(result);

        MatrixBuilder builder = new MatrixBuilder(G, Z);

        //Stamp resistors
        for (Resistor r : result.resistors) {
            builder.stampResistor(r.getPinA(), r.getPinB(), r.getConductance());
        }
        //Stamp the voltage sources.
        {
            //Used to dislocate the numbers of nodes in circuit,
            // so can directly stamp the matrix without using another matrix to help.
            int index = result.nodes;
            for (VoltageSource vs : result.voltage_source) {
                builder.stampVoltageSource(vs.getPinA(), vs.getPinB(), index++, vs.getVoltageDifference());
            }
        }

        //Stamp the current sources.
        for (CurrentSource cs : result.current_sources) {
            builder.stampCurrentSource(cs.getPinA(), cs.getPinB(), cs.getCurrent());
        }

        for (Capacitor c : result.capacitors) {
            builder.stampCapacitor(c.getPinA(), c.getPinB(), c.getCapacitance() / TIME_STEP, c.getCurrent());
        }

        //D matrix will be implemented soon.
        for (int m0 = result.nodes; m0 < nm; m0++) {
            for (int m1 = result.nodes; m1 < nm; m1++) {
                G[m0][m1] = 0;
            }
        }

        this.G = G;
        this.inverse = Tools.invert(Tools.deepCopy(G));
        this.Z = Z;
        solveInitialConditions(result, Tools.deepCopy(G), Tools.deepCopy(Z));
    }

    /**
     * Due to the differential equations nature of the capacitors and inductors, we need to compute an additional step.<br>
     * This extra step will calculate the initial values at (t = -1) to ensure the precision at t = 0.<br>
     *  Ex: In an RC circuit, the expected initial current and voltage across the capacitor is ic = δ/R, U = 0.
     * But without this method called before the simulation step, the current value will be wrongly computed
     * because the initial voltage differential at t = -1 doesn't exist. Then this method ensures that the initial voltage across the capacitor in t = -1 will be 0.
     */
    private void solveInitialConditions(AnalysesResult result, double[][] g, double[] z){
        //To a capacitor that we use the companion method to simulate it in the matrix,
        // use R ~= 0 ensure then the voltage across the resistor will be next to 0, simulating a discharged capacitor.
        MatrixBuilder builder = new MatrixBuilder(g.clone(), z.clone());
        for (Capacitor c : result.capacitors) {
            //Set max conductance to simulate an open circuit.
            builder.stampResistor(c.getPinA(), c.getPinB(), Double.MAX_VALUE);
            //Current can't flow yet, so set to 0.
            //builder.stampCurrentSource(c.getPinA(), c.getPinB(), 0);
        }
        double[][] inverted = Tools.invert(builder.G);
        //Inject values in pointers.
        double[] initial = Tools.multiply(inverted, inverted.length, builder.Z);
        reInjectFromCalculatedValues(initial);

        //At this point (t-1), the initial voltage and current states on nodes and voltage sources is available, and correctly computed.
        for (Simulable simulable : simulableElements) {
            //A non-time tick, so the elements can inject the initial values.
            simulable.doInitialTick(this);
        }
    }

    public void dirt() {
        isDirt = true;
    }

    private void clean() {
        checkInconsistencies();
        buildMatrix();
        isDirt = false;
    }

    /**
     * Values like the voltage in 'n' node needs to be injected in the components.<br>
     * The Voltage source needs the current to calculate the power.
     * The Current source needs the potential difference to calculate the power.
     * The resistor needs potential difference to calculate the current and the power.
     */
    private void reInjectFromCalculatedValues(double[] values){
        for (int i = 0; i < X.length; i++) {
            X[i][0] = values[i];
        }
    }

    public void preCompile(){
        clean();
    }

    public void tick(double dt) {
        for (Simulable element : simulableElements)
            element.tick(TIME_STEP, this);
        double[] values = Tools.multiply(inverse, inverse.length, Z);
        reInjectFromCalculatedValues(values);
    }

    public Element[] getElements() {
        return elements.toArray(new Element[0]);
    }

    /**
     * Get a clone of X matrix.
     */
    public double[][] getX() {
        return X.clone();
    }

    /**
     * Get a clone of Z matrix.
     */
    public double[] getZ() {
        return Z.clone();
    }

    /**
     * Get a clone of G matrix.
     */
    public double[][] getG() {
        return G.clone();
    }

    public MatrixBuilder getMatrixBuilder(){
        return new MatrixBuilder(G, Z);
    }
}
