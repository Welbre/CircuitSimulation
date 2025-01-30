package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.Element.Pin;
import kuse.welbre.sim.electricalsim.tools.CircuitAnalyser;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;
import kuse.welbre.sim.electricalsim.tools.Tools;

import java.util.*;

public class Circuit {
    /** 5ms time step */
    public static final double TIME_STEP = 0.005;

    private final List<Element> elements = new ArrayList<>();
    private final List<Simulable> simulableElements = new ArrayList<>();

    private MatrixBuilder matrixBuilder;
    /**
     * This array storage 'n' nodes voltage pointers and 'm' current throw a voltage source.<br>
     * The array it's a double[n+m][1], each index represents a double pointer, so this allows passing a reference for each element
     * and modify only once.
     */
    private double[][] X;

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

    /**
     * Convert the random pins address to a workable value, the address is some short number between 0 and {@link Short#MAX_VALUE}.<br>
     * Set the pointers in the {@link Pin pin}, and set the voltageSource current pointer.
     */
    private void preparePinsAndSources(CircuitAnalyser.Result result, double[][] X){
        short next = 0;
        for (Pin pin : result.pins) {
            //Set to useful index in a matrix.
            pin.address = next;
            //Set the pointer address to the same in X matrix.
            pin.P_voltage = X[next++];
        }

        //Dislocate result.nodes from the top of Z matrix, to set the voltage sources values in the correct row.
        short index = (short) result.nodes;
        for (int i = 0; i < result.voltage_source.size(); i++) {
            //Set the current pointer.
            result.voltage_source.get(i).setCurrentPointer(X[i + result.pins.size()]);
            result.voltage_source.get(i).address = index++;
        }
    }

    public void buildMatrix() {
        final CircuitAnalyser.Result result = CircuitAnalyser.analyseCircuit(this);
        final int nm = result.nodes + result.voltage_source.size();

        final double[][] G = new double[nm][nm];
        final double[] Z = new double[nm];
        X = new double[nm][1];
        preparePinsAndSources(result, X);

        matrixBuilder = new MatrixBuilder(G, Z);

        //Stamp resistors
        for (Resistor r : result.resistors)
            matrixBuilder.stampResistor(r.getPinA(), r.getPinB(), r.getConductance());
        //Stamp the voltage sources.
        for (VoltageSource vs : result.voltage_source)
            matrixBuilder.stampVoltageSource(vs.getPinA(), vs.getPinB(), vs.address, vs.getSourcesVoltage());
        //Stamp the current sources.
        for (CurrentSource cs : result.current_sources)
            matrixBuilder.stampCurrentSource(cs.getPinA(), cs.getPinB(), cs.getCurrent());
        //Stamp the capacitors
        for (Capacitor c : result.capacitors)
            matrixBuilder.stampCapacitor(c.getPinA(), c.getPinB(), c.getCapacitance() / TIME_STEP, c.getCurrent());

        //D matrix will be implemented soon.
        for (int m0 = result.nodes; m0 < nm; m0++) {
            for (int m1 = result.nodes; m1 < nm; m1++) {
                G[m0][m1] = 0;
            }
        }

        matrixBuilder.close();
        solveInitialConditions(result, matrixBuilder.copy());
    }

    /**
     * Due to the differential equations nature of the capacitors and inductors, we need to compute an additional step.<br>
     * This extra step will calculate the initial values at (t = -1) to ensure the precision at t = 0.<br>
     *  Ex: In an RC circuit, the expected initial current and voltage across the capacitor is ic = δ/R, U = 0.
     * But without this method called before the simulation step, the current value will be wrongly computed
     * because the initial voltage differential at t = -1 doesn't exist. Then this method ensures that the initial voltage across the capacitor in t = -1 will be 0.
     */
    private void solveInitialConditions(CircuitAnalyser.Result result, MatrixBuilder builder){
        //To a capacitor that we use the companion method to simulate it in the matrix,
        // use R ~= 0 ensure then the voltage across the resistor will be next to 0, simulating a discharged capacitor.
        for (Capacitor c : result.capacitors) {
            //Set max conductance to simulate an open circuit.
            builder.stampResistor(c.getPinA(), c.getPinB(), Double.MAX_VALUE);
            //Current can't flow yet, so set to 0.
            //builder.stampCurrentSource(c.getPinA(), c.getPinB(), 0);
        }

        builder.close();
        //Inject values in pointers.
        double[] initial = builder.getResult();
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
        double[] values = matrixBuilder.getResult();
        reInjectFromCalculatedValues(values);
    }

    public Element[] getElements() {
        return elements.toArray(new Element[0]);
    }

    public MatrixBuilder getMatrixBuilder(){
        return matrixBuilder;
    }

    /**
     * Get a clone of X matrix.
     */
    public double[][] getX() {
        return Tools.deepCopy(X);
    }

    /**
     * Get a clone of Z matrix.
     */
    public double[] getZ() {
        return matrixBuilder.getCopyOfZ();
    }

    /**
     * Get a clone of G matrix.
     */
    public double[][] getG() {
        return matrixBuilder.getCopyOfG();
    }
}
