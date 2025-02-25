package kuse.welbre.sim;

import kuse.welbre.sim.electrical.*;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.Diode;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import kuse.welbre.tools.LU;
import kuse.welbre.tools.Tools;

import java.io.PrintStream;
import java.util.Arrays;

public class Main {
    //Diode test
    public static void main(String[] args) {
        Circuit circuit = new Circuit();
        VoltageSource v = new VoltageSource(10);
        Resistor r = new Resistor(1);
        Diode d = new Diode(); //n = 1, sat = 1pA

        v.connect(r.getPinA(), null);
        d.connect(r.getPinB(), null);

        circuit.addElement(v,r,d);

        for (int i = 0; i < 50; i++) {
            circuit.tick(0);
            printAllElements(circuit);
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