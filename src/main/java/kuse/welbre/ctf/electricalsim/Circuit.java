package kuse.welbre.ctf.electricalsim;

import kuse.welbre.ctf.electricalsim.Element.Pin;
import kuse.welbre.ctf.electricalsim.tools.Tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Circuit implements Simulable {
    private final List<Element> elements = new ArrayList<>();
    private double[][] inverse = null;
    /**
     * Hold the known values.
     */
    private double[] z = null;
    /**
     * This array storage 'n' nodes voltage pointers and 'm' current throw a voltage source.<br>
     * The array it's a double[n+m][1], each index represents a double pointer, so this allows passing a reference for each element
     * and modify only once.
     */
    private double[][] X = null;

    public double[][] DEBUG_G;
    public double[][] DEBUG_X;
    public double[] DEBUG_Z;

    private boolean isDirt = true;

    @SafeVarargs
    public final <T extends Element> void addElement(T... element) {
        for (T ele : element)
            if (!elements.contains(ele))
                elements.add(ele);
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
        public final Set<Pin> pins;

        public AnalysesResult(Set<Pin> pins, List<VoltageSource> voltage_source, List<CurrentSource> current_sources, List<Resistor> resistors) {
            //removes the ground from the list, because it isn't having relevance in the matrix.
            pins.remove(null);
            this.nodes = pins.size();
            this.voltage_source = voltage_source;
            this.current_sources = current_sources;
            this.resistors = resistors;
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

        for (Element element : elements) {
            pins.add(element.getPinA());
            pins.add(element.getPinB());
            if (element instanceof VoltageSource vs)
                voltageSources.add(vs);
            if (element instanceof CurrentSource cs)
                currentSources.add(cs);
            if (element instanceof Resistor r)
                resistors.add(r);
        }

        return new AnalysesResult(pins, voltageSources, currentSources, resistors);
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

    public void buildMatrix() {
        final AnalysesResult result = analyseNodesAndElements();
        final int nm = result.nodes + result.voltage_source.size();
        final double[][] G = new double[nm][nm];
        final double[] Z = new double[nm];
        X = new double[nm][1];
        preparePinsAndSources(result);

        //Stamp resistors in G matrix.
        for (Resistor r : result.resistors) {
            Pin a = r.getPinA(); Pin b = r.getPinB();
            final double g = r.getConductance();
            if (a != null) {
                G[a.address][a.address] += g;
                if (b != null) {
                    G[a.address][b.address] -= g;
                    G[b.address][a.address] -= g;
                }
            }
            if (b != null) {
                G[b.address][b.address] += g;
            }
        }
        //Stamp the voltage sources in G matrix.
        {
            int index = result.nodes;
            for (VoltageSource vs : result.voltage_source) {
                Pin a = vs.getPinA(); Pin b = vs.getPinB();
                if (a != null) {
                    G[a.address][index] = 1;
                    G[index][a.address] = 1;
                }
                if (b != null) {
                    G[b.address][index] = -1;
                    G[index][b.address] = -1;
                }
                index++;
            }
        }

        //D matrix will be implemented soon.
        for (int m0 = result.nodes; m0 < nm; m0++) {
            for (int m1 = result.nodes; m1 < nm; m1++) {
                G[m0][m1] = 0;
            }
        }

        //Populate the Z matrix
        {
            for (CurrentSource cs : result.current_sources) {
                Pin a = cs.getPinA(); Pin b = cs.getPinB();
                if (a != null)
                    Z[a.address] += cs.getCurrent();
                if (b != null)
                    Z[b.address] -= cs.getCurrent();
            }
            int index = result.nodes;
            for (VoltageSource vs : result.voltage_source) {
                Z[index++] = vs.getVoltageDifference();
            }
        }

        //lu = Tools.calculate_lu(G, nm);
        inverse = Tools.invert(G);
        z = Z;
        //todo remove when is done, this line above is only to debug reasons.
        DEBUG_G = G; DEBUG_X = X; DEBUG_Z = Z;
    }

    public void dirt() {
        isDirt = true;
    }

    private void clean() {
        if (!isDirt) return;
        checkInconsistencies();
        buildMatrix();
    }

    /**
     * Values like the voltage in 'n' node needs to be injected in the components.<br>
     * The Voltage source needs the current to calculate the power.
     * The Current source needs the potential difference to calculate the power.
     * The resistor needs potential difference to calculate the current and the power.
     */
    private void reInjectFromCalculatedValues(){
        double[] values = Tools.multiply(inverse, inverse.length, z);
        for (int i = 0; i < X.length; i++) {
            X[i][0] = values[i];
        }
    }

    @Override
    public void tick(double dt) {
        if (isDirt)
            clean();
        reInjectFromCalculatedValues();
    }

    public Element[] getElements() {
        return elements.toArray(new Element[0]);
    }
}
