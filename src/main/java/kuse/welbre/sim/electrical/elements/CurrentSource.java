package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.MatrixBuilder;

@SuppressWarnings("unused")
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
    public double getQuantity() {
        return current;
    }

    @Override
    public String getQuantitySymbol() {
        return "A";
    }
}
