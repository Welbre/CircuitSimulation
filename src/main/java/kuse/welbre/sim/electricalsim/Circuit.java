package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.Element.Pin;
import kuse.welbre.sim.electricalsim.tools.CircuitAnalyser;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;
import kuse.welbre.sim.electricalsim.tools.Tools;

import java.io.PrintStream;
import java.util.*;

public class Circuit {
    /// 50ms time step
    public static final double TICK_TO_SOLVE_INITIAL_CONDITIONS = 1E-5;
    public static final double DEFAULT_TIME_STEP = 0.05;
    private double tickRate = DEFAULT_TIME_STEP;
    /// Max conductance value to avoid a Nan sum of 2 or more {@link Double#MAX_VALUE} resulting in inf.
    public static final double MAX_CONDUCTANCE = 1E10;

    private final List<Element> elements = new ArrayList<>();
    private final List<Simulable> simulableElements = new ArrayList<>();

    private CircuitAnalyser analyseResult;
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
    private void preparePinsAndSources(CircuitAnalyser result, double[][] X){
        short next = 0;
        for (Pin pin : result.pins) {
            //Set to useful index in a matrix.
            pin.address = next;
            //Set the pointer address to the same in X matrix.
            pin.P_voltage = X[next++];
        }

        //Dislocate result.nodes from the top of Z matrix, to set the voltage sources values in the correct row.
        short index = (short) result.nodes;
        for (int i = 0; i < result.voltageSources.size(); i++) {
            //Set the current pointer.
            result.voltageSources.get(i).setCurrentPointer(X[i + result.pins.size()]);
            result.voltageSources.get(i).address = index++;
        }
    }

    public void buildMatrix() {
        if (analyseResult == null)
            throw new IllegalStateException("Build matrix called before circuit analysis!");

        final int nm = analyseResult.nodes + analyseResult.voltageSources.size();

        final double[][] G = new double[nm][nm];
        final double[] Z = new double[nm];
        X = new double[nm][1];
        preparePinsAndSources(analyseResult, X);

        matrixBuilder = new MatrixBuilder(G, Z);

        //Stamp resistors
        for (Resistor r : analyseResult.resistors)
            matrixBuilder.stampResistor(r.getPinA(), r.getPinB(), r.getConductance());
        //Stamp the voltage sources.
        for (VoltageSource vs : analyseResult.voltageSources)
            matrixBuilder.stampVoltageSource(vs.getPinA(), vs.getPinB(), vs.address, vs.getVoltageDifference());
        //Stamp the current sources.
        for (CurrentSource cs : analyseResult.currentSources)
            matrixBuilder.stampCurrentSource(cs.getPinA(), cs.getPinB(), cs.getCurrent());
        //Stamp the capacitors.
        for (Capacitor c : analyseResult.capacitors)
            matrixBuilder.stampCapacitor(c.getPinA(), c.getPinB(), c.getCapacitance() / getTickRate(), c.getCurrent());
        //Stamp inductors.
        for (Inductor l : analyseResult.inductors)
            matrixBuilder.stampInductor(l.getPinA(), l.getPinB(), getTickRate() / l.getInductance(), l.getCurrent());

        //D matrix will be implemented soon.
        for (int m0 = analyseResult.nodes; m0 < nm; m0++) {
            for (int m1 = analyseResult.nodes; m1 < nm; m1++) {
                G[m0][m1] = 0;
            }
        }

        matrixBuilder.close();
    }

    /**
     * Due to the differential equations nature in dynamic elements as capacitor and inductors,
     * and backwards Euler method that is used to solve it, we need to compute an additional step.<br>
     * Backwards euler method computes an approximation of derivatives using the preview values, so as the simulation starts in t = 0, it is impossible to compute derivatives because the t = -1 not exists.<br>
     * This extra step will initiate all electrical elements at t = 0, and compute a tiny step forwards to compute the derivatives, this step is so tiny that we can approximate it to 0
     * thus, defining the initial conditions at t = 0.
     */
    private void solveInitialConditions(){
        final int nm = analyseResult.nodes + analyseResult.voltageSources.size();
        MatrixBuilder builder = new MatrixBuilder(new double[nm][nm],new double[nm]);

        final double originalTickRate = getTickRate();
        this.tickRate = Circuit.TICK_TO_SOLVE_INITIAL_CONDITIONS;

        //Stamp resistors
        for (Resistor r : analyseResult.resistors)
            builder.stampResistor(r.getPinA(), r.getPinB(), r.getConductance());
        for (VoltageSource vs : analyseResult.voltageSources)
            builder.stampVoltageSource(vs.getPinA(), vs.getPinB(),vs.address, 0);

        //This sim uses backwards Euler method to discretize the capacitors and inductors, and create a companion model that consists of a current source and a parallel resistor.
        //As the timeStep that we use approaches to 0, the companion resistor of a capacitor model gets close to 0, and the companion resistor of inductor model gets close to infinite.
        for (Capacitor c : this.analyseResult.capacitors)
            //As the conductance is the inverse of resistance, when r approaches to 0, (g approaches to infinite) simulating a short circuit.
            //But is important to keep in mind that float point numbers have major problems with infinite values and NaN,
            //instead add an infinite conductance; we add a fix number to simulate the short circuit.
            builder.stampResistor(c.getPinA(), c.getPinB(), c.getCapacitance() / getTickRate());
        for (Inductor l : this.analyseResult.inductors)
            //The inductor is the same idea, but at t = 0, the indicator act as an open circuit (G = 0).
            builder.stampResistor(l.getPinA(), l.getPinB(), getTickRate() / l.getInductance());

        //initial the elements to solve initial conditions.
        for (Simulable simulable : simulableElements)
            simulable.initiate(this);

        builder.close();

        for (double ramp = 0; ramp < 1; ramp += 0.1) {
            Arrays.fill(matrixBuilder.getZ(), 0);

            //Re stamp fixes sources
            for (var vs : analyseResult.voltageSources)
                matrixBuilder.stampZMatrixVoltageSource(vs.address, vs.getVoltageDifference() * ramp);
            for (var cs : analyseResult.currentSources)
                matrixBuilder.stampCurrentSource(cs.getPinA(), cs.getPinB(), cs.getCurrent() * ramp);

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
        this.tickRate = originalTickRate;
    }

    public void tick(double dt) {
        if (isDirt)
            clean();
        else {
            Arrays.fill(matrixBuilder.getZ(), 0);

            //Re stamp fixes sources
            for (var vs : analyseResult.voltageSources)
                matrixBuilder.stampZMatrixVoltageSource(vs.address, vs.getVoltageDifference());
            for (var cs : analyseResult.currentSources)
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
        analyseResult = new CircuitAnalyser(this);

        try {checkInconsistencies();} catch (RuntimeException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Circuit with inconsistencies!");
        }

        buildMatrix();
        solveInitialConditions();

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

    public void exportToSpiceNetlist(PrintStream stream){
        if (this.analyseResult == null)
            throw new IllegalStateException("Matrix need's to be build before run this method.");

        stream.println("Exported by Welber's Circuit sim");
        {
            int idx = 1;
            for (VoltageSource source : analyseResult.voltageSources)
                stream.printf("V%d %s %s %s\n",idx++, source.getPinA() == null ? 0 : "N" + (source.getPinA().address + 1), source.getPinB() == null ? 0 : "N" + (source.getPinB().address + 1), source.getPropriety());
        }
        {
            int idx = 1;
            for (CurrentSource source : analyseResult.currentSources)
                stream.printf("I%d %s %s %s\n",idx++, source.getPinA() == null ? 0 : "N" + (source.getPinA().address + 1), source.getPinB() == null ? 0 : "N" + (source.getPinB().address + 1), source.getPropriety());
        }
        {
            int idx = 1;
            for (Resistor source : analyseResult.resistors)
                stream.printf("R%d %s %s %s\n",idx++, source.getPinA() == null ? 0 : "N" + (source.getPinA().address + 1), source.getPinB() == null ? 0 : "N" + (source.getPinB().address + 1), source.getPropriety());
        }
        {
            int idx = 1;
            for (Capacitor source : analyseResult.capacitors)
                stream.printf("C%d %s %s %s\n",idx++, source.getPinA() == null ? 0 : "N" + (source.getPinA().address + 1), source.getPinB() == null ? 0 : "N" + (source.getPinB().address + 1), source.getPropriety());
        }
        {
            int idx = 1;
            for (Inductor source : analyseResult.inductors)
                stream.printf("L%d %s %s %s\n",idx++, source.getPinA() == null ? 0 : "N" + (source.getPinA().address + 1), source.getPinB() == null ? 0 : "N" + (source.getPinB().address + 1), source.getPropriety());
        }
        stream.println(".tran 0 1 0 0.005 startup\n.backanno\n.end");
    }
}
