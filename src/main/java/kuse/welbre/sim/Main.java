package kuse.welbre.sim;

import kuse.welbre.sim.electrical.*;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.tools.LU;
import kuse.welbre.tools.Tools;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.Function;

import static java.lang.Math.*;

public class Main {
    ////x^3 - 3x*y^2 = 1
    ////3yx^2 - y^3 = 0

    public static double[] rhs = new double[]{1,0};

    public static double[][] jacobian(double[] u){
        double x = u[0], y = u[1];
        double[][] jacobian = new double[2][2];

        // Partial derivatives for f1 = x^3 - 3*x*y^2 - 1
        jacobian[0][0] = 3 * x * x;   // df1/dx
        jacobian[0][1] = -6 * x * y;  // df1/dy

        // Partial derivatives for f2 = 3*y*x^2 - y^3
        jacobian[1][0] = 6 * x * y;   // df2/dx
        jacobian[1][1] = 3 * x * x - 3 * y * y; // df2/dy

        return jacobian;
    }

    public static double[] f(double[] u){
        double x = u[0], y = u[1];
        double[] values = new double[2];

        values[0] = Math.pow(x, 3) - 3 * x * Math.pow(y, 2);
        values[1] = 3 * y * Math.pow(x, 2) - Math.pow(y, 3);

        return Tools.subtract(values, rhs);
    }

    public static boolean isValidResult(double[] result, double tolerance){
        return Tools.norm(result) < tolerance;
    }

    public static boolean isConverging(double[] a, double[] b){
        return Tools.norm(a) < Tools.norm(b);
    }

    //Diode test
    public static void main(String[] args) {
        double[] x = new double[]{375,-895};//0,0 initial guesses
        double[] dx = new double[]{Double.MAX_VALUE, Double.MAX_VALUE};
        double[] fx = f(x);
        int i = 0;

        for (; i < 5000; i++) {
            if (isValidResult(fx, 1e-6) || isValidResult(dx, 1e-6))
                break;

            dx = LU.decompose(jacobian(x)).solve(fx);

            //Resolve non a number
            boolean needReset = false;
            for (int j = 0; j < dx.length; j++) {
                if (!Double.isFinite(dx[j])) {
                    x[j] += 1e-12;

                    needReset = true;
                }
            }
            if (needReset){
                dx = LU.decompose(jacobian(x)).solve(fx);
                continue;
            }

            System.out.print(Arrays.toString(dx));

            double[] t = Tools.subtract(x, dx);
            double[] ft = f(t);
            double a = 1;
            int sub = 0; //Infinite control.

            while (!isConverging(ft,fx)){
                a /= 2.0;
                t = Tools.subtract(x, Tools.multiply(dx,a));
                ft = f(t);
                System.out.print("#");
                if (++sub > 500)
                    throw new RuntimeException("Non divergent! " + a);
            }
            dx = Tools.subtract(x, t);
            x = t;
            fx = ft;
            System.out.print("\n");
        }
        System.out.println("Solution: " + Arrays.toString(x));
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