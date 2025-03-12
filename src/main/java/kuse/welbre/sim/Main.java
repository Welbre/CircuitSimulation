package kuse.welbre.sim;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Capacitor;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.sim.electrical.elements.VoltageSource;

import java.io.PrintStream;

public class Main {

    public static void main(String[] args) {
        Circuit c = new Circuit();
        VoltageSource v = new VoltageSource(10);
        Resistor r = new Resistor(10);
        Capacitor l = new Capacitor(0.5);


        c.addElement(v,r,l);
        v.connect(r.getPinA(), null);
        l.connect(r.getPinB(),null);

        c.preCompile();
        System.out.println(l);
        for (int i = 0; i < 50; i++) {
            c.tick(0);
            printAllElements(c);
        }
    }

    //Lu solve
    /*
    public static void main(String[] args) {
        double[][] circuit = new double[][]{{1, -1, 0, 1}, {-1, 3, -1, 0}, {0, -1, 2, 0}, {1, 0, 0, 0}};
        LU decompose = LU.decompose(circuit);
        double[][] inv = decompose.inverse();

        System.out.println(Arrays.toString(Tools.multiply(inv, 4, new double[]{0, 0, 0, 10})));
        System.out.println(Arrays.toString(Tools.multiply(Tools.invert(circuit), 4, new double[]{0, 0, 0, 10})));
    }

     */

    public static void printCircuitMatrix(Circuit matrix, PrintStream stream){
        stream.println("G matrix:");
        printMatrix(matrix.getG(), stream);
        stream.println("X matrix:");
        printMatrix(matrix.getX(), stream);
        stream.println("Z matrix:");
        printMatrix(matrix.getZ(), stream);
    }

    public static void printCircuitMatrix(Circuit matrix){
        printCircuitMatrix(matrix, System.out);
    }

    public static void printMatrix(double[][] matrix, PrintStream stream){
        StringBuilder builder = new StringBuilder();
        for (double[] doubles : matrix) {
            for (double aDouble : doubles) {
                builder.append(String.format("%.3f", aDouble)).append("\t\t\t");
            }
            builder.append("\n");
        }
        stream.println(builder);
    }

    public static void printMatrix(double[][] matrix) {
        printMatrix(matrix, System.out);
    }

    public static void printMatrix(double[] matrix, PrintStream stream){
        StringBuilder builder = new StringBuilder();
        for (double v : matrix) {
            builder.append(String.format("%.3f", v)).append("\n");
        }
        stream.println(builder);
    }

    public static void printMatrix(double[] matrix) {
        printMatrix(matrix, System.out);
    }

    public static void printAllElements(Circuit circuit, PrintStream stream) {
        for (Element element : circuit.getElements()) {
            stream.println(element);
        }
    }

    public static void printAllElements(Circuit circuit) {
        printAllElements(circuit, System.out);
    }

    public static void printX(Circuit circuit, PrintStream stream) {
        for (int i = 0; i < circuit.getX().length; i++) {
            stream.printf("%.3f\t",circuit.getX()[i][0]);
        }
    }

    public static void printX(Circuit circuit) {
        printX(circuit, System.out);
    }
}