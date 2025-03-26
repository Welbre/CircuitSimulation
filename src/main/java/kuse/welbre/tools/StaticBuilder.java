package kuse.welbre.tools;

import kuse.welbre.sim.electrical.CircuitAnalyser;
import kuse.welbre.sim.electrical.abstractt.Element.Pin;

public class StaticBuilder {
    protected final double[][] lhs;

    protected boolean isClosed = false;

    public StaticBuilder(CircuitAnalyser analyser) {
        this(new double[analyser.matrixSize][analyser.matrixSize]);
    }

    public StaticBuilder(double[][] lhs) {
        this.lhs = lhs;
    }

    public void stampResistence(Pin a, Pin b, double resistence){
        stampConductance(a,b, 1.0 / resistence);
    }

    public void stampConductance(Pin a, Pin b, double conductance) {
        if (isClosed)
            throw new IllegalStateException("Try stamp a conductance in a closed builder!");
        if (a != null) {
            lhs[a.address][a.address] += conductance;
            if (b != null) {
                lhs[a.address][b.address] -= conductance;
                lhs[b.address][a.address] -= conductance;
            }
        }
        if (b != null) {
            lhs[b.address][b.address] += conductance;
        }
    }

    public void stampVoltageSource(Pin a, Pin b, int sor_idx){
        if (isClosed) throw new IllegalStateException("Try stamp a voltage source in a closed builder!");
        if (a != null) {
            lhs[a.address][sor_idx] = 1;
            lhs[sor_idx][a.address] = 1;
        }
        if (b != null) {
            lhs[b.address][sor_idx] = -1;
            lhs[sor_idx][b.address] = -1;
        }
    }

    public void stampLHS(int row, int colum, double value){
        if (isClosed) throw new IllegalStateException("Try stamp left hand side in a closed builder!");
        lhs[row][colum] = value;
    }

    public void close(){
        isClosed = true;
    }

    public double[][] getLhs() {
        return lhs;
    }
}
