package kuse.welbre.sim.electricalsim.tools;

import kuse.welbre.sim.electricalsim.Element;

/**
 * An aid to build the G matrix, uses null to connect the element to ground.
 */

public final class MatrixBuilder {
    private final double[][] G;
    /**
     * Hold the known values.
     */
    private final double[] Z;
    private double[][] inverse = null;

    private boolean isClosed = false;

    public MatrixBuilder(double[][] g, double[] z) {
        G = g;
        Z = z;
    }

    public void stampResistor(Element.Pin a, Element.Pin b, double conductance) {
        if (isClosed) throw new IllegalStateException("Try stamp a resistor in a closed builder!");
        if (a != null) {
            G[a.address][a.address] += conductance;
            if (b != null) {
                G[a.address][b.address] -= conductance;
                G[b.address][a.address] -= conductance;
            }
        }
        if (b != null) {
            G[b.address][b.address] += conductance;
        }
    }

    public void stampVoltageSource(Element.Pin a, Element.Pin b, int nodes_length, double voltage){
        if (isClosed) throw new IllegalStateException("Try stamp a voltage source in a closed builder!");
        if (a != null) {
            G[a.address][nodes_length] = 1;
            G[nodes_length][a.address] = 1;
        }
        if (b != null) {
            G[b.address][nodes_length] = -1;
            G[nodes_length][b.address] = -1;
        }
        Z[nodes_length] = voltage;
    }

    public void stampCurrentSource(Element.Pin a, Element.Pin b, double current){
        if (a != null)
            Z[a.address] += current;
        if (b != null)
            Z[b.address] -= current;
    }

    public void stampCapacitor(Element.Pin a, Element.Pin b, double capacitance_per_tick, double current){
        stampResistor(a, b, capacitance_per_tick);
        stampCurrentSource(a, b, current);
    }

    public void close(){
        if (isClosed) return;
        inverse = Tools.invert(Tools.deepCopy(G));
        isClosed = true;
    }

    /**
     * @return The result of multiplication between {@link MatrixBuilder#inverse} and {@link MatrixBuilder#Z}, casual named X matrix.
     */
    public double[] getResult(){
        if (!isClosed)
            throw new IllegalStateException("Try get result in a non closed matrix builder!");
        return Tools.multiply(inverse, inverse.length, Z);
    }

    public double[][] getInverse() {
        if (!isClosed)
            throw new IllegalStateException("Try get inverse in a non closed matrix builder!");
        return inverse;
    }

    public double[][] getG() {
        return G;
    }

    public double[] getZ() {
        return Z;
    }

    public double[][] getCopyOfG() {
        return Tools.deepCopy(G);
    }

    public double[] getCopyOfZ() {
        return Tools.deepCopy(Z);
    }

    ///Returns a copy of this. Do a deep copy of all matrices, and return an opening matrix builder.
    public MatrixBuilder copy() {
        return new MatrixBuilder(Tools.deepCopy(this.G), Tools.deepCopy(this.Z));
    }
}