package kuse.welbre.tools;

import kuse.welbre.sim.electrical.CircuitAnalyser;
import kuse.welbre.sim.electrical.abstractt.Element;
import org.apache.commons.math4.legacy.linear.MatrixUtils;

import java.util.Arrays;

public class MatrixBuilder {
    protected final double[][] LHS;
    protected final double[] RHS;
    protected LU lu;

    private boolean isClosed = false;

    public MatrixBuilder(CircuitAnalyser analyser) {
        this(
                new double[analyser.matrixSize][analyser.matrixSize],
                new double[analyser.matrixSize]
        );
    }

    public MatrixBuilder(double[][] LHS, double[] RHS) {
        this.LHS = LHS;
        this.RHS = RHS;
    }

    ///Returns a copy of builder. Do a deep copy of all matrices, and return an opening matrix builder.
    public MatrixBuilder(MatrixBuilder builder) {
        this.LHS = Tools.deepCopy(builder.LHS);
        this.RHS = Tools.deepCopy(builder.RHS);
        this.lu = builder.lu;
        this.isClosed = false;
    }

    public void stampResistence(Element.Pin a, Element.Pin b, double resistence){
        stampConductance(a,b, 1.0 / resistence);
    }

    public void stampConductance(Element.Pin a, Element.Pin b, double conductance) {
        if (isClosed)
            throw new IllegalStateException("Try stamp a conductance in a closed builder!");
        if (a != null) {
            LHS[a.address][a.address] += conductance;
            if (b != null) {
                LHS[a.address][b.address] -= conductance;
                LHS[b.address][a.address] -= conductance;
            }
        }
        if (b != null) {
            LHS[b.address][b.address] += conductance;
        }
    }

    public void stampVoltageSource(Element.Pin a, Element.Pin b, int nodes_length, double voltage){
        if (isClosed) throw new IllegalStateException("Try stamp a voltage source in a closed builder!");
        if (a != null) {
            LHS[a.address][nodes_length] = 1;
            LHS[nodes_length][a.address] = 1;
        }
        if (b != null) {
            LHS[b.address][nodes_length] = -1;
            LHS[nodes_length][b.address] = -1;
        }
        RHS[nodes_length] = voltage;
    }

    public void stampCurrentSource(Element.Pin a, Element.Pin b, double current){
        if (isClosed) throw new IllegalStateException("Try stamp right hand side in a closed builder!");

        if (a != null)
            RHS[a.address] += current;
        if (b != null)
            RHS[b.address] -= current;
    }

    public void stampLHS(int row, int colum, double value){
        if (isClosed) throw new IllegalStateException("Try stamp left hand side in a closed builder!");
        LHS[row][colum] = value;
    }

    public void stampRHS(int row, double value){
        RHS[row] = value;
    }

    public void clearZMatrix(){
        Arrays.fill(RHS, 0);
    }

    public void close(){
        if (isClosed) return;
        //todo implement a better solver
        lu = LU.decompose(LHS);
        isClosed = true;
    }

    /**
     * @return The equation system result, using {@link LU} and {@link NonLinearMatrixBuilder#RHS}
     */
    public double[] getResult(){
        if (!isClosed)
            throw new IllegalStateException("Try get result in a non closed matrix builder!");
        return lu.solve(RHS);
    }

    public double[][] getLHS() {
        return LHS;
    }

    public double[] getRHS() {
        return RHS;
    }

    public double[][] getCopyOfLHS() {
        return Tools.deepCopy(LHS);
    }

    public double[] getCopyOfRHS() {
        return Tools.deepCopy(RHS);
    }
}
