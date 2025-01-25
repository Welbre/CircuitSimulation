package kuse.welbre.ctf.electricalsim;

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
    public double getPower() {
        return getVoltageDifference() * getCurrent();
    }

    @Override
    protected double getPropriety() {
        return current;
    }
}
