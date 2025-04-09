package kuse.welbre.sim.electrical;

import com.jogamp.common.util.ReflectionUtil;
import kuse.welbre.sim.electrical.abstractt.*;
import kuse.welbre.tools.LU;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

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

    ///ensures that pins with the same address are point to the same memory location.
    private void mergePins(){
        for (int i = 0; i < elements.size()-1; i++) {
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
                elements_per_pin.get(pin == null ? gnd : pin).add(element);

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
        mergePins();
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

        var map = new LinkedHashMap<Class<? extends Element>,List<Element>>();
        for (Element element : elements) {//sort based on element class
            map.putIfAbsent(element.getClass(), new ArrayList<>());
            map.get(element.getClass()).add(element);
        }
        {//write all elements
            final byte idx_deep = (byte) Math.ceil(elements.size()/255.0);
            var set = map.entrySet();
            st.writeInt(elements.size());//put the number of elements in total.
            st.writeByte(idx_deep);//write numbers of bytes to write the element index.
            st.writeInt(set.size());//put the amount of types.

            for (var entry : set) {
                var list = entry.getValue();
                st.writeShort(SerialTypeEnum.fromClass(entry.getKey()));//write the code of class
                st.writeInt(list.size());//write the number of elements of this class
                for (Element l : list) {
                    //todo key eyes on it, with circuit with more that 255 elements of a type, this will be crash.
                    if (idx_deep == 1)//put the idx of each element
                        st.writeByte(elements.indexOf(l));
                    else if (idx_deep == 2)
                        st.writeShort(elements.indexOf(l));
                    else if (idx_deep == 3 || idx_deep == 4)
                        st.writeInt(elements.indexOf(l));
                    else
                        throw new RuntimeException("Circuit is to big to be serialized!");

                    l.serialize(st);//write the element itself
                }
            }
        }
    }

    @Override
    public void unSerialize(DataInputStream st) throws IOException {
        setTickRate(st.readDouble());
        {//read all elements
            Element[] elements_array = new Element[st.readInt()];
            final byte idx_deep = st.readByte();
            try {
                int elements_size = st.readInt();//read the amount of element types
                for (int i = 0; i < elements_size; i++) {
                    Class<? extends Element> element_class;
                    {//get the element class
                        short class_idx = st.readShort();
                        if (class_idx == -1)
                            throw new RuntimeException("SerialType %d is invalid!!!".formatted(class_idx));
                        element_class = SerialTypeEnum.values()[class_idx].aClass;
                    }

                    int amount = st.readInt();//gets the amount of element of this class
                    for (int j = 0; j < amount; j++) {
                        final int idx;
                        if (idx_deep == 1)//put the idx of each element
                            idx = st.readUnsignedByte();
                        else if (idx_deep == 2)
                            idx = st.readUnsignedShort();
                        else if (idx_deep == 3 || idx_deep == 4)
                            idx = st.readInt();
                        else
                            throw new RuntimeException("Circuit is to big to be serialized!");
                        Element element = element_class.getConstructor().newInstance();
                        elements_array[idx] = element;

                        element.unSerialize(st);
                    }
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
            addElement(elements_array);
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

        /**
         * The voltage difference.
         * Assuming that voltage in K is bigger than L. Therefore, current flows from K to L.
         */
        public static double GET_VOLTAGE_DIFF(Pin k, Pin l){
            double K = 0, L = 0;
            if (k != null)
                if (k.P_voltage == null)
                    return Double.NaN;
                else
                    K = k.P_voltage[0];

            if (l != null)
                if (l.P_voltage == null)
                    return Double.NaN;
                else
                    L = l.P_voltage[0];

            return K-L;
        }
    }
}
