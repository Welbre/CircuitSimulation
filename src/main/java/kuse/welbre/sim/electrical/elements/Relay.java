package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Element4Pin;
import kuse.welbre.sim.electrical.abstractt.Operational;
import kuse.welbre.tools.MatrixBuilder;

@SuppressWarnings("unused")
public class Relay extends Element4Pin implements Operational, Dynamic {
    public static final double DEFAULT_OPERATIONAL_CURRENT = 1e-1;//100mA
    public static final double DEFAULT_OPERATIONAL_TIME = 1e-2;//10ms
    public static final double DEFAULT_OPERATIONAL_VOLTAGE = 12;//12V

    private boolean isOpen = true;
    private boolean dirt = true;
    private double closedResistence = 1e-6;//1mΩ default
    private double openResistence = 1e6;//1MΩ default
    private double operationalCurrent = DEFAULT_OPERATIONAL_CURRENT;
    private final Inductor inductor = Element.asField(() -> new Inductor(getPinC(), getPinD(), DEFAULT_OPERATIONAL_VOLTAGE * DEFAULT_OPERATIONAL_TIME / DEFAULT_OPERATIONAL_CURRENT));

    public Relay() {
    }

    public Relay(double closedResistence) {
        this.closedResistence = closedResistence;
    }

    public Relay(double closedResistence, double openResistence) {
        this.closedResistence = closedResistence;
        this.openResistence = openResistence;
    }

    public Relay(double closedResistence, boolean isOpen) {
        this.closedResistence = closedResistence;
        this.isOpen = isOpen;
    }

    public Relay(Pin pinA, Pin pinB, Pin pinC, Pin pinD) {
        super(pinA, pinB, pinC, pinD);
        inductor.connect(pinC, pinD);
    }

    public Relay(Pin pinA, Pin pinB, Pin pinC, Pin pinD, double closedResistence, boolean isOpen) {
        super(pinA, pinB, pinC, pinD);
        this.closedResistence = closedResistence;
        this.isOpen = isOpen;
    }

    @Override
    public void initiate(Circuit circuit) {
        inductor.initiate(circuit);
    }

    @Override
    public void preEvaluation(MatrixBuilder builder) {
        inductor.preEvaluation(builder);
    }

    @Override
    public void posEvaluation(MatrixBuilder builder) {
        inductor.posEvaluation(builder);
        final boolean shouldOpen = inductor.getCurrent() < operationalCurrent;
        if (isOpen != shouldOpen) {//check if is needed to change the state.
            isOpen = shouldOpen;
            dirt();
        }
    }

    @Override
    public double getMinTickRate() {
        return inductor.getMinTickRate();
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        builder.stampResistence(getPinA(), getPinB(), isOpen ? openResistence : closedResistence);
        inductor.stamp(builder);
        dirt = false;
    }





    public double getOperationalCurrent() {
        return operationalCurrent;
    }

    public void setOperationalCurrent(double operationalCurrent) {
        this.operationalCurrent = operationalCurrent;
    }

    @Override
    public double[]getProperties() {
        return new double[]{closedResistence, openResistence};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"Ω","Ω"};
    }

    @Override
    public double getCurrent() {
        //only use isOpen start if isn't dirt
        return getVoltageDifference() / (isOpen ^ dirt ? openResistence : closedResistence);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public double getOpenResistence() {
        return openResistence;
    }

    public double getClosedResistence() {
        return closedResistence;
    }

    public void setOpenResistence(double openResistence) {
        this.openResistence = openResistence;
        dirt();
    }

    public void setCloseResistence(double closeResistence) {
        this.closedResistence = closeResistence;
        dirt();
    }

    @Override
    public boolean isDirt() {
        return dirt;
    }

    @Override
    public void dirt() {
        dirt = true;
    }

    @Override
    public void connectC(Pin pin) {
        super.connectC(pin);
        inductor.connectA(pin);
    }

    @Override
    public void connectD(Pin pin) {
        super.connectD(pin);
        inductor.connectB(pin);
    }
}
