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
    private final List<Element> elements;

    public NonLinearHelper(MatrixBuilder builder, List<Element> elements) {
        this.original_builder = builder;
        this.elements = elements;
    }

    public double[][] jacobian(){
        MatrixBuilder builder = new MatrixBuilder(original_builder);
        for (Element e : elements)
            if (e instanceof NonLinear nonLinear)
                builder.stampConductance(e.getPinA(), e.getPinB(), Math.max(nonLinear.plane_dI_dV(e.getVoltageDifference()),1e-12));

        return builder.getLHS();
    }

    public double[] f(double[] x){
        MatrixBuilder builder = new MatrixBuilder(original_builder);
        for (Element e : elements)
            if (e instanceof NonLinear nonLinear)
                builder.stampCurrentSource(e.getPinA(), e.getPinB(), -nonLinear.plane_I_V(e.getVoltageDifference()));//parei aqui

        return Tools.subtract(Tools.multiply(builder.getLHS(),x), builder.getRHS());
    }
}
