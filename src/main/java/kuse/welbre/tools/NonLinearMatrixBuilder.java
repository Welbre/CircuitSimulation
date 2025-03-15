package kuse.welbre.tools;

import kuse.welbre.sim.electrical.CircuitAnalyser;
import kuse.welbre.sim.electrical.abstractt.Element;

/**
 * An aid to build the G matrix, uses null to connect the element to ground.
 */

public final class NonLinearMatrixBuilder extends MatrixBuilder {
    private final boolean lhs_is_static;
    private final boolean rhs_is_static;

    private boolean isClosed = false;

    public NonLinearMatrixBuilder(CircuitAnalyser analyser) {
        this(
            new double[analyser.matrixSize][analyser.matrixSize],
            new double[analyser.matrixSize],
            analyser
        );
    }

    public NonLinearMatrixBuilder(double[][] LHS, double[] RHS, CircuitAnalyser analyser) {
        super(LHS, RHS);
        lhs_is_static = !analyser.isNonLinear;
        rhs_is_static = !analyser.isDynamic;
    }

    ///Returns a copy of builder. Do a deep copy of all matrices, and return an opening matrix builder.
    public NonLinearMatrixBuilder(NonLinearMatrixBuilder builder) {
        super(builder);
        this.lhs_is_static = builder.lhs_is_static;
        this.rhs_is_static = builder.rhs_is_static;
        this.isClosed = false;
    }

    public void stampConductance(Element.Pin a, Element.Pin b, double conductance) {
        if (isClosed)
            throw new IllegalStateException("Try stamp a resistor in a closed builder!");
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
        if (isClosed && lhs_is_static)
            throw new IllegalStateException("Try stamp a voltage source in a closed builder!");
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
        if (rhs_is_static && isClosed)
            throw new IllegalStateException("Try stamp right hand side in a closed builder!");
        if (a != null)
            RHS[a.address] += current;
        if (b != null)
            RHS[b.address] -= current;
    }

    public void stampLHS(int row, int colum, double value){
        if (isClosed && lhs_is_static) throw new IllegalStateException("Try stamp left hand side in a closed builder!");
        LHS[row][colum] = value;
    }

    public void stampRHS(int row, double value){
        if (isClosed && rhs_is_static) throw new IllegalStateException("Try stamp right hand side in a closed builder!");
        RHS[row] = value;
    }
}