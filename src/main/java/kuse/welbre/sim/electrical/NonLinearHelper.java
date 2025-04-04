package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.util.List;

/**
 * An aid to solve non-linear circuits.
 */
public class NonLinearHelper {
    private final MatrixBuilder builder;
    private final List<NonLinear> nonLinear;

    public NonLinearHelper(MatrixBuilder builder, List<NonLinear> nonLinear) {
        this.builder = builder;
        this.nonLinear = nonLinear;
    }

    public double[][] jacobian(){
        builder.clearLhs();
        for (NonLinear e : nonLinear)
            e.stamp_dI_dV(builder);

        return builder.getLhs();
    }

    public double[] f(double[] x){
        builder.clear();
        for (NonLinear e : nonLinear)
            e.stamp_I_V(builder);

        return Tools.subtract(Tools.multiply(builder.getLhs(),x), builder.getRhs());
    }
}
