package kuse.welbre.sim;

import kuse.welbre.sim.electricalsim.*;
import kuse.welbre.sim.electricalsim.exemples.Circuits;

import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        Circuit circuit = Circuits.Inductors.getAssociationCircuit();
        circuit.preCompile();

        circuit.exportToSpiceNetlist(System.out);
    }

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