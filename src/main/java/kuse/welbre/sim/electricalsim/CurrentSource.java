package kuse.welbre.sim.electricalsim;

public class CurrentSource extends Element {
    private double current;

    public CurrentSource() {
    }

    public CurrentSource(double current) {
        this.current = current;
    }

    public CurrentSource(Pin nodeA, Pin nodeB, double current) {
        super(nodeA, nodeB);
        this.current = current;
    }

    public double getCurrent() {
        return current;
    }

    @Override
    protected double getPropriety() {
        return current;
    }

    @Override
    protected String getProprietySymbol() {
        return "A";
    }
}
