package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.Element.Pin;
import kuse.welbre.sim.electricalsim.tools.CircuitAnalyser;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;
import kuse.welbre.sim.electricalsim.tools.Tools;

import java.io.PrintStream;
import java.util.*;

public class Circuit {
    /// 50ms time step
    public static final double TICK_TO_SOLVE_INITIAL_CONDITIONS = 1E-10;
    public static final double DEFAULT_TIME_STEP = 0.05;
    private double tickRate = DEFAULT_TIME_STEP;

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
        //initiation
        Pin gnd = new Pin();
        HashMap<Pin, List<Element>> elements_per_pin = new HashMap<>();

        elements_per_pin.put(gnd, new ArrayList<>());
        for (Pin pin : this.analyseResult.pins)
            elements_per_pin.put(pin, new ArrayList<>());

        for (Element element : elements) {
            elements_per_pin.get(element.getPinA() == null ? gnd : element.getPinA()).add(element);
            elements_per_pin.get(element.getPinB() == null ? gnd : element.getPinB()).add(element);
        }

        //Error check
        for (Map.Entry<Pin, List<Element>> entry : elements_per_pin.entrySet()) {
            Pin key = entry.getKey(); List<Element> list = entry.getValue();

            if (entry.getValue().isEmpty())
                throw new IllegalStateException(String.format("%s have no connections! possible fault in circuit formation.", key));

            if (entry.getValue().size() == 1) {
                Element element = list.getFirst();
                throw new IllegalStateException(String.format("%s(%s)[%s,%s] is connected to %s without a path, possible fault in circuit formation!",
                        element.getClass().getSimpleName(),
                        Tools.proprietyToSi(element.getPropriety(), element.getProprietySymbol(), 2),
                        element.getPinA() == null ? "gnd" : element.getPinA().address,
                        element.getPinB() == null ? "gnd" : element.getPinB().address,
                        key));
            }
        }
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

        //Init simulable
        for (Simulable s : simulableElements)
            s.initiate(this);

        //Stamp and init all elements
        for (Element e : elements)
            e.stamp(matrixBuilder);

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

        //initial the elements to solve initial conditions.
        for (Simulable simulable : simulableElements)
            simulable.initiate(this);

        //Stamp
        for (Element e : elements)
            e.stamp(builder);

        builder.close();

        //preTick in t
        for (var sim : simulableElements)
            sim.preEvaluation(matrixBuilder);

        //Calculate the values and inject in pointers.
        double[] initial = builder.getResult();
        injectValuesInX(initial);

        //Pos tick
        for (var sim : simulableElements)
            sim.posEvaluation(matrixBuilder);


        this.tickRate = originalTickRate;
        //Start simulable elements with original tick rate.
        for (Simulable simulable : simulableElements)
            simulable.initiate(this);
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
