package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.abstractt.Element;
import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

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
    public void stamp(MatrixBuilder builder) {
        builder.stampCurrentSource(this.getPinA(), this.getPinB(), this.getCurrent());
    }

    @Override
    protected double getQuantity() {
        return current;
    }

    @Override
    protected String getQuantitySymbol() {
        return "A";
    }
}
