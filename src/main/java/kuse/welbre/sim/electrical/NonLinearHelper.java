package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Simulable;
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
    private final List<Simulable> simulableElements;

    public NonLinearHelper(Circuit circuit, int size, List<Element> elements, List<Simulable> simulableElements) {
        this.circuit = circuit;
        this.size = size;
        this.elements = elements;
        this.simulableElements = simulableElements;
    }

    public double[][] jacobian(){
        MatrixBuilder nonBuilder = new MatrixBuilder(new double[size][size],new double[size]);

        for (Simulable simulable : simulableElements)
            simulable.initiate(circuit);
        for (Element e : elements) {
            if (e instanceof Diode diode){
                nonBuilder.stampResistor(diode.getPinA(), diode.getPinB(), Math.max(diode.plane_dI_dV(diode.getVoltageDifference()),1e-12));
            }
            else
                e.stamp(nonBuilder);
        }

        return nonBuilder.getG();
    }

    public double[] f(double[] x){
        MatrixBuilder nonBuilder = new MatrixBuilder(new double[size][size],new double[size]);

        for (Simulable simulable : simulableElements)
            simulable.initiate(circuit);
        for (Element e : elements) {
            if (e instanceof Diode diode){
                nonBuilder.stampCurrentSource(diode.getPinA(), diode.getPinB(), -diode.plane_I_V(diode.getVoltageDifference()));
            }
            else
                e.stamp(nonBuilder);
        }

        return Tools.subtract(Tools.multiply(nonBuilder.getG(),x),nonBuilder.getZ());
    }
}
