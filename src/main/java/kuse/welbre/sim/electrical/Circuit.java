package kuse.welbre.sim.electrical;

import com.jogamp.common.util.ReflectionUtil;
import kuse.welbre.sim.electrical.abstractt.*;
import kuse.welbre.tools.LU;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Circuit implements Serializable {
    /// 50ms time step
    public static final double TICK_TO_SOLVE_INITIAL_CONDITIONS = 1E-10;
    public static final double DEFAULT_TIME_STEP = 0.05;

    private double tickRate = DEFAULT_TIME_STEP;
    private double minimal_time_step = Double.MAX_VALUE;

    private final List<Element> elements = new ArrayList<>();
    private final List<Dynamic> dynamics = new ArrayList<>();
    private final List<NonLinear> nonLiners = new ArrayList<>();
    private final List<Operational> operationals = new ArrayList<>();
    private final List<Watcher<?>> watchers = new ArrayList<>();

    private CircuitAnalyser analyseResult;
    private MatrixBuilder matrixBuilder;
    /**
     * This array storage 'n' nodes voltage pointers and 'm' current throw a voltage source.<br>
     * The array it's a double[n+m][1], each index represents a double pointer, so this allows passing a reference for each element
     * and modify only once.
     */
    private double[][] X;

    private boolean isDirt = true;

    public final void addElement(Element element){
        if (!this.elements.contains(element)) {
            this.elements.add(element);

            if (element instanceof Dynamic sim)
                dynamics.add(sim);
            if (element instanceof NonLinear non)
                nonLiners.add(non);
            if (element instanceof Operational op)
                operationals.add(op);
        }
    }

    @SafeVarargs
    public final <T extends Element> void addElement(T... elements) {
        for (T element : elements)
            addElement(element);
    }

    public final <T extends Element> void addElement(Collection<T> elements){
        for (T element : elements)
            addElement(element);
    }

    @SafeVarargs
    public final <T extends Element> void addWatcher(Watcher<T>... watchers) {
        this.watchers.addAll(Arrays.asList(watchers));
    }

    /**
     * Try to find if some node or element can crash the matrix generation.
     */
    private void checkInconsistencies() {
        //initiation
        Pin gnd = new Pin();
        HashMap<Pin, List<Element>> elements_per_pin = new HashMap<>();

        //All pins to map
        elements_per_pin.put(gnd, new ArrayList<>());
        for (Pin pin : this.analyseResult.pins)
            elements_per_pin.put(pin, new ArrayList<>());

        //Add all elements to correspondent pins.
        for (Element element : elements)
            for (Pin pin : element.getPins())
                elements_per_pin.get(pin == null ? gnd : pin).add(element);//is crashing because used memory address to compare the element, instead the address field.

        //Error check
        for (Map.Entry<Pin, List<Element>> entry : elements_per_pin.entrySet()) {
            Pin key = entry.getKey(); List<Element> list = entry.getValue();

            if (entry.getValue().isEmpty())
                throw new IllegalStateException(String.format("%s have no connections! possible fault in circuit formation.", key));

            if (entry.getValue().size() == 1) {
                Element element = list.getFirst();
                throw new IllegalStateException(String.format("%s[%s,%s] is connected to %s without a path, possible fault in circuit formation!",
                        element.getClass().getSimpleName(),
                        element.getPinA() == null ? "gnd" : element.getPinA().address,
                        element.getPinB() == null ? "gnd" : element.getPinB().address,
                        key));
            }
        }
    }

    /**
     * Convert the random pins address to a workable value, the address is some short number between 0 and {@link Short#MAX_VALUE}.<br>
     * Defines the minimal time step in the circuit.<br>
     * Set the pointers in the {@link Pin pin}, and set the voltageSource current pointer.
     */
    private void prepareToBuild(CircuitAnalyser result, double[][] X){
        short next = 0;
        for (Pin pin : result.pins) {
            //Set to useful index in a matrix.
            pin.address = next;
            //Set the pointer address to the same in X matrix.
            pin.P_voltage = X[next++];
        }

        //Dislocate result.nodes from the top of Z matrix, to set the voltage sources values in the correct row.
        short index = (short) result.nodes;
        //Set the RHSElements
        for (Element e : elements){
            if (e instanceof RHSElement rhs){
                rhs.setValuePointer(X[index]);
                rhs.setAddress(index++);
            } else if (e instanceof MultipleRHSElement mRHS) {
                double[][] pointers = new double[mRHS.getRHSAmount()][1];
                short[] address = new short[mRHS.getRHSAmount()];
                for (int i = 0; i < mRHS.getRHSAmount(); i++){
                    pointers[i] = X[index];
                    address[i] = index++;
                }
                mRHS.setValuePointer(pointers);
                mRHS.setAddress(address);
            }
        }

        //Search in dynamics elements list for the smallest time.
        for (Dynamic dynamic : dynamics) {
            double rate = dynamic.getMinTickRate();
            if (rate < minimal_time_step)
                minimal_time_step = rate;
        }
        if (minimal_time_step < tickRate)
            tickRate = minimal_time_step;
    }

    private void buildMatrix() {
        matrixBuilder = new MatrixBuilder(analyseResult);

        //Init simulable
        for (Dynamic s : dynamics)
            s.initiate(this);

        //Stamp and init all elements
        for (Element e : elements)
            if (!(e instanceof NonLinear))
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
        final double originalTickRate = getTickRate();
        this.tickRate = Circuit.TICK_TO_SOLVE_INITIAL_CONDITIONS;
        MatrixBuilder builder = new MatrixBuilder(analyseResult);
        //todo maybe mark the capacitor and inductors as a volatile element, therefore will be stamped in the overload instead of original LHS (only because the inductance / tickRate ?).

        //initial the elements to solve initial conditions.
        for (Dynamic dynamic : dynamics)
            dynamic.initiate(this);

        //dirt all operational to re-stamp.
        for (Operational op : operationals)
            op.dirt();

        for (var sim : dynamics)//preTick in t
            sim.preEvaluation(builder);

        //tick in t
        if (analyseResult.isNonLinear) {
            for (Element e : elements)//stamp
                if (!(e instanceof NonLinear))
                    e.stamp(builder);
            builder.close();

            solveNonLinear(builder);
        } else {
            for (Element e : elements)//stamp
                e.stamp(builder);
            builder.close();

            injectValuesInX(builder.getResult());
        }

        //Pos tick
        for (var sim : dynamics)
            sim.posEvaluation(builder);

        this.tickRate = originalTickRate;
        //Start simulable elements with original tick rate.
        for (Dynamic dynamic : dynamics)
            dynamic.initiate(this);
    }

    private void solveNonLinear(MatrixBuilder original){
        //--------------------------------------------------------------
        //This entire block is to linearize the non-linear components.
        //--------------------------------------------------------------
        original.lock();
        NonLinearHelper nl = new NonLinearHelper(original, nonLiners);
        double[] x = new double[X.length]; //initial x in the X point.
        double[] dx = new double[X.length];// initial dx with max double value.
        double[] fx;
        int inter = 0;

        //initiate x and dx.
        for (int i = 0; i < X.length; i++) {
            x[i] = X[i][0];
            dx[i] = Double.MAX_VALUE;
        }

        fx = nl.f(x); //compute the initial fx

        for (; inter < 500; inter++){
            if (Tools.norm(fx) < 1e-6 || Tools.norm(dx) < 1e-6)//check for convergence.
                break;

            injectValuesInX(x);//update pointer values to the components compute using x
            dx = LU.decompose(Tools.deepCopy(nl.jacobian())).solve(Tools.deepCopy(fx));//get the jacobian and find a dx that solves J(u)*u=f(u).

            double[] t = Tools.subtract(x, dx); //calculate t the new x

            injectValuesInX(t);//update again now using t, the new x
            double[] ft = nl.f(t);//calculate the f(t)
            double a = 1;
            int subinter = 0;

            while (Tools.norm(ft) >= Tools.norm(fx) && subinter < 50){//check the system is converging to 0, the new point f(t) < f(x)
                a /= 2.0;//if it isn't converging, reduce the step by half.
                t = Tools.subtract(x, Tools.multiply(dx,a));//compute the new subT, t = x - a*dx
                injectValuesInX(t);//update values in component to subT.
                ft = nl.f(t);//re compute f(t) now using the subT

                subinter++;
            }

            dx = Tools.subtract(x,t);//recompute the dx. if the initial t isn't converging, the t was updated inside the loop, and we need to calculate dx again using the new t.
            x = t;//the new guess is t.
            fx = ft;//how t is the new guess, update f(guess) to f(t).
        }
        if (inter == 500)
            throw new IllegalStateException("The circuit can't converge!");

        original.unlock();
        original.clear();
    }

    ///Tick one time step
    public void tick(){
        if (isDirt)
            clean();
        else {
            for (var op : operationals) {
                if (op.isDirt()) {
                    buildMatrix();
                    break;
                }
            }

            //we are in t, so use actual values to prepare evaluation to t+1
            for (Dynamic element : dynamics)
                element.preEvaluation(matrixBuilder);

            if (analyseResult.isNonLinear) {
                solveNonLinear(matrixBuilder);
            }
            else
                injectValuesInX(matrixBuilder.getResult());//Evaluation from t to t + 1

            //Pos ticking to calculate values in t + 1
            for (var sim : dynamics)
                sim.posEvaluation(matrixBuilder);

            //run watchers
            for (Watcher<?> watcher : watchers)
                watcher.run();
        }
    }


    /**
     * Simulate 'time' seconds.
     * @param t_sec total time in seconds to simulate.
     */
    public void tick(double t_sec) {
        double t = 0;
        while (t_sec > t){
            tick();
            t += tickRate;
        }
    }


    public void dirt() {
        isDirt = true;
    }

    private void clean() {
        //ensure that pins with the same address are point to the same memory location.
        for (int i = 0; i < elements.size()-1; i++) {//todo put in a method
            Pin[] outPins = elements.get(i).getPins();
            for (Pin out : outPins) {
                if (out == null) continue;

                for (int j = i + 1; j < elements.size(); j++) {
                    Pin[] innerPin = elements.get(j).getPins();
                    for (int j1 = 0; j1 < innerPin.length; j1++) {
                        Pin in = innerPin[j1];
                        if (in == null)
                            continue;
                        if (out.address == in.address)
                            elements.get(j).connect(out, j1);
                    }
                }
            }
        }
        analyseResult = new CircuitAnalyser(this);

        try {checkInconsistencies();} catch (IllegalStateException e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Circuit with inconsistencies!");
        }

        final int size = analyseResult.matrixSize;
        X = new double[size][1];
        prepareToBuild(analyseResult, X);

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
        solveInitialConditions();
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
        return matrixBuilder.getCopyOfRHS();
    }

    /**
     * Get a clone of G matrix.
     */
    public double[][] getG() {
        return matrixBuilder.getLhs();
    }

    public void setTickRate(double tickRate) {
        if (tickRate <= 0)
            throw new IllegalArgumentException("Tick rate must be bigger that 0!");
        if (tickRate > minimal_time_step)
            throw new IllegalStateException("Tick rate must be less that (%s) duo some Dynamic element requirement.".formatted(Tools.proprietyToSi(minimal_time_step, "s")));
        this.tickRate = tickRate;
        dirt();
    }

    public double getTickRate(){
        return tickRate;
    }

    @Override
    public void serialize(DataOutputStream st) throws IOException {
        st.writeDouble(tickRate);
        var map = new HashMap<Class<? extends Element>,List<Element>>();

        for (Element element : elements) {//Organize based on element class
            map.putIfAbsent(element.getClass(), new ArrayList<>());
            map.get(element.getClass()).add(element);
        }
        {//write to stream
            var set = map.entrySet();
            st.writeInt(set.size());
            for (var entry : set) {
                var ls = entry.getValue();
                st.write(entry.getKey().getName().concat("\0").getBytes(StandardCharsets.US_ASCII));//todo use a light approach to write the class name.
                st.writeInt(ls.size());
                for (Element l : ls)
                    l.serialize(st);
            }
        }
    }

    @Override
    public void unSerialize(DataInputStream st) throws IOException {
        setTickRate(st.readDouble());
        //read all elements
        try {
            int elements_size = st.readInt();
            for (int i = 0; i < elements_size; i++) {
                StringBuilder className = new StringBuilder();
                {//read a string using the zero char as stop point.
                    byte next;
                    while ((next = st.readByte()) != 0) {
                        className.append(new String(new byte[]{next}, StandardCharsets.US_ASCII));
                    }
                }

                Class<? extends Element> element_class;
                {//get the element class using the class name
                    Class<?> fromName = Class.forName(className.toString());
                    if (Element.class.isAssignableFrom(fromName)) {
                        element_class = (Class<? extends Element>) fromName;//is safe
                    } else
                        throw new RuntimeException("Class %s isn't a element class!".formatted(className));
                }
                int amount = st.readInt();
                for (int j = 0; j < amount; j++) {
                    Element element = element_class.getConstructor().newInstance();
                    element.unSerialize(st);
                    this.addElement(element);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Pin {
        private static final Random rand = new Random();
        public short address;
        public double[] P_voltage = null;

        public Pin(short node) {
            this.address = node;
        }

        public Pin() {
            address = (short) rand.nextInt();
        }

        @Override
        public String toString() {
            return "Pin[" + address + "]";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Pin pin && pin.address == this.address;
        }

        @Override
        public int hashCode() {
            return address;//todo this is not the ideal, create one system that is fast, light, and can be easily replaced by the current one.
        }

        /**
         * The voltage difference.
         * Assuming that voltage in K is bigger than L. Therefore, current flows from K to L.
         */
        public static double GET_VOLTAGE_DIFF(Pin k, Pin l){
            double K = 0, L = 0;
            if (k != null)
                if (k.P_voltage == null)
                    throw new IllegalStateException("Try get a voltage in a non initialized pin(A)!");//todo pass a NAN instead a exception.
                else
                    K = k.P_voltage[0];

            if (l != null)
                if (l.P_voltage == null)
                    throw new IllegalStateException("Try get a voltage in a non initialized pin(B)!");
                else
                    L = l.P_voltage[0];

            return K-L;
        }
    }
}
