package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.util.List;

/**
 * An aid to solve non-linear circuits.
 */
public class NonLinearHelper {
    private final MatrixBuilder original_builder;
    private final List<NonLinear> nonLinear;

    public NonLinearHelper(MatrixBuilder builder, List<NonLinear> nonLinear) {
        this.original_builder = builder;
        this.nonLinear = nonLinear;
    }

    public double[][] jacobian(){
        MatrixBuilder builder = new MatrixBuilder(original_builder);
        for (NonLinear e : nonLinear)
            e.stamp_dI_dV(builder);

        return builder.getLHS();
    }

    public double[] f(double[] x){
        MatrixBuilder builder = new MatrixBuilder(original_builder);

        for (NonLinear e : nonLinear)
            e.stamp_I_V(builder);

        return Tools.subtract(Tools.multiply(builder.getLHS(),x), builder.getRHS());
    }
}
