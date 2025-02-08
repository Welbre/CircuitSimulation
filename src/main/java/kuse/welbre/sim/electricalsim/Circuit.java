package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.Element.Pin;
import kuse.welbre.sim.electricalsim.tools.CircuitAnalyser;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;
import kuse.welbre.sim.electricalsim.tools.Tools;

import java.io.PrintStream;
import java.util.*;

public class Circuit {
    /// 50ms time step
    public static final double DEFAULT_TIME_STEP = 0.05;
    private double tickRate = DEFAULT_TIME_STEP;
    /// Max conductance value to avoid a Nan sum of 2 or more {@link Double#MAX_VALUE} resulting in inf.
    public static final double MAX_CONDUCTANCE = 1E10;

    private final List<Element> elements = new ArrayList<>();
    private final List<Simulable> simulableElements = new ArrayList<>();

    private CircuitAnalyser.Result analyseResult;
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
    private boolean checkInconsistencies() {
        //todo need's to be implemented.
        return true;
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
        final var result = CircuitAnalyser.analyseCircuit(this);
        final int nm = result.nodes + result.voltage_source.size();

        final double[][] G = new double[nm][nm];
        final double[] Z = new double[nm];
        X = new double[nm][1];
        preparePinsAndSources(result, X);

        matrixBuilder = new MatrixBuilder(G, Z);
        analyseResult = result;

        //Stamp resistors
        for (Resistor r : result.resistors)
            matrixBuilder.stampResistor(r.getPinA(), r.getPinB(), r.getConductance());
        //Stamp the voltage sources.
        for (VoltageSource vs : result.voltage_source)
            matrixBuilder.stampVoltageSource(vs.getPinA(), vs.getPinB(), vs.address, vs.getVoltageDifference());
        //Stamp the current sources.
        for (CurrentSource cs : result.current_sources)
            matrixBuilder.stampCurrentSource(cs.getPinA(), cs.getPinB(), cs.getCurrent());
        //Stamp the capacitors.
        for (Capacitor c : result.capacitors)
            matrixBuilder.stampCapacitor(c.getPinA(), c.getPinB(), c.getCapacitance() / getTickRate(), c.getCurrent());
        //Stamp inductors.
        for (Inductor l : result.inductors)
            matrixBuilder.stampInductor(l.getPinA(), l.getPinB(), getTickRate() / l.getInductance(), l.getCurrent());

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
        for (Capacitor c : result.capacitors)
            //Set high conductance to simulate short closed circuit.
            builder.stampResistor(c.getPinA(), c.getPinB(), Circuit.MAX_CONDUCTANCE);
        //To the inductor is the same principle, but in t = -1, the inductor acts as open circuit, so remove the conductance added in the preview method.
        for (var l : result.inductors)
            //Get the opposite inductance per tick to remove the conductance added previously and multiply by 0.99999999 to get a value close to 0 but not zero.
            builder.stampResistor(l.getPinA(), l.getPinB(), (-getTickRate() / l.getInductance()));

        //Start simulable elements
        for (Simulable simulable : simulableElements)
            simulable.initiate(this);

        builder.close();

        //preTick in t
        for (var sim : simulableElements)
            sim.preEvaluation(matrixBuilder);

        //Calculate the values and inject in pointers.
        double[] initial = builder.getResult();
        injectValuesInX(initial);
    }

    public void tick(double dt) {
        if (isDirt)
            clean();
        else {
            Arrays.fill(matrixBuilder.getZ(), 0);

            //Re stamp fixes sources
            for (var vs : analyseResult.voltage_source)
                matrixBuilder.stampZMatrixVoltageSource(vs.address, vs.getVoltageDifference());
            for (var cs : analyseResult.current_sources)
                matrixBuilder.stampCurrentSource(cs.getPinA(), cs.getPinB(), cs.getCurrent());
            //we are in t, so use actual values to prepare evaluation to t+1
            for (Simulable element : simulableElements)
                element.preEvaluation(matrixBuilder);

            //above is t
            injectValuesInX(matrixBuilder.getResult());//Evaluation from t to t + 1
            //below is t + 1

            //Pos ticking to calculate values in t + 1
            for (var sim : simulableElements)
                sim.posEvaluation(matrixBuilder);
        }
    }


    public void dirt() {
        isDirt = true;
    }

    private void clean() {
        if (! checkInconsistencies())
            throw new RuntimeException("Circuit with inconsistencies");
        buildMatrix();
        isDirt = false;
    }

    /**
     * Values like the voltage in 'n' node needs to be injected in the components.<br>
     * The Voltage source needs the current to calculate the power.
     * The Current source needs the potential difference to calculate the power.
     * The resistor needs potential difference to calculate the current and the power.
     */
    private void injectValuesInX(double[] values){
        for (int i = 0; i < X.length; i++) {
            X[i][0] = values[i];
        }
    }

    public void preCompile(){
        clean();
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

    public void setTickRate(double tickRate) {
        if (tickRate <= 0) throw new IllegalArgumentException("Tick rate must be bigger that 0!");
        this.tickRate = tickRate;
        dirt();
    }

    public double getTickRate(){
        return tickRate;
    }

    public void printCircuitText(PrintStream stream){
        if (this.analyseResult == null)
            throw new IllegalStateException("Matrix need's to be build before run this method.");
        for (Element element : elements) {
            short a = -1, b = -1;
            if (element.getPinA() != null)
                a = element.getPinA().address;
            if (element.getPinB() != null)
                b = element.getPinB().address;

            if (element instanceof VoltageSource)
                stream.println("Vs " + (a == -1 ? "null" : a) + " " + (b == -1 ? "null" : b) + " " + element.getPropriety());
            else if (element instanceof CurrentSource)
                stream.println("Cs " + (a == -1 ? "null" : a) + " " + (b == -1 ? "null" : b) + " " + element.getPropriety());
            else if (element instanceof Resistor)
                stream.println("R " + (a == -1 ? "null" : a) + " " + (b == -1 ? "null" : b) + " " + element.getPropriety());
            else if (element instanceof Capacitor)
                stream.println("C " + (a == -1 ? "null" : a) + " " + (b == -1 ? "null" : b) + " " + element.getPropriety());
            else if (element instanceof Inductor)
                stream.println("L " + (a == -1 ? "null" : a) + " " + (b == -1 ? "null" : b) + " " + element.getPropriety());
        }
    }
}
