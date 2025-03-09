package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Dynamic;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.sim.electrical.elements.Capacitor;
import kuse.welbre.sim.electrical.elements.Diode;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.util.List;

/**
 * An aid to solve non-linear circuits.
 */
public class NonLinearHelper {
    private final Circuit circuit;
    private final int size;
    private final List<Element> elements;
    private final List<Dynamic> dynamics;

    public NonLinearHelper(Circuit circuit, int size, List<Element> elements, List<Dynamic> dynamics) {
        this.circuit = circuit;
        this.size = size;
        this.elements = elements;
        this.dynamics = dynamics;
    }

    public double[][] jacobian(){
        MatrixBuilder nonBuilder = new MatrixBuilder(new double[size][size],new double[size]);

        for (Dynamic element : dynamics)
            element.preEvaluation(nonBuilder);
        for (Element e : elements)
            if (e instanceof NonLinear nonLinear)
                nonBuilder.stampResistor(e.getPinA(), e.getPinB(), Math.max(nonLinear.plane_dI_dV(e.getVoltageDifference()),1e-12));
            else
                e.stamp(nonBuilder);

        return nonBuilder.getG();
    }

    public double[] f(double[] x, Element.Pin a, Element.Pin b, double value){
        MatrixBuilder nonBuilder = new MatrixBuilder(new double[size][size],new double[size]);
        nonBuilder.stampCurrentSource(a,b,value);

        for (Dynamic element : dynamics)
            if (!(element instanceof Capacitor))
                element.preEvaluation(nonBuilder);
        for (Element e : elements)
            if (e instanceof NonLinear nonLinear)
                nonBuilder.stampCurrentSource(e.getPinA(), e.getPinB(), -nonLinear.plane_I_V(e.getVoltageDifference()));
            else
                e.stamp(nonBuilder);

        return Tools.subtract(Tools.multiply(nonBuilder.getG(),x),nonBuilder.getZ());
    }
}
