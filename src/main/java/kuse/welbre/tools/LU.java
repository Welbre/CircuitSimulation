package kuse.welbre.tools;

import java.util.Arrays;

public final class LU {
    public final double[][] l;
    public final double[][] u;

    public LU(double[][] l, double[][] u) {
        this.l = l;
        this.u = u;
    }

    public static LU decompose(double[][] A){
        final double[][] l = identity(A.length);
        final double[][] u = Tools.deepCopy(A);
        LU lu = new LU(l,u);

        for (int row = 1; row < u.length; row++) {
            for (int colum = 0; colum < row; colum++) {
                final double d = u[row][colum];
                if (d == 0) {
                    l[row][colum] = 0;
                    continue;
                }
                int pivot = -1;
                { //try swap

                }
                final double mult = - d / u[colum][colum];//diagonal as pivot.
                l[row][colum] = -mult;

                u[row][colum] = 0;
                for (int _colum = colum+1; _colum < u.length; _colum++)
                    u[row][_colum] += mult * u[colum][_colum];
            }
        }

        return lu;
    }

    public double[] solve(double[] rhs){
        int size = this.l.length;
        double[] y = new double[size];
        double[] x = new double[size];

        //Solve LY = rhs to find y
        for (int row = 0; row < size; row++) {
            double summation = 0;
            for (int colum = 0; colum < row; colum++)
                summation += this.l[row][colum] * y[colum];
            y[row] = (rhs[row] - summation);
        }

        //Solve UX=Y to find x
        for (int row = size-1; row >= 0; row--) {
            double summation = 0;
            for (int colum = size-1; colum > row; colum--)
                summation += this.u[row][colum] * x[colum];
            x[row] = (y[row] - summation) / this.u[row][row];
        }
        return x;
    }

    public double[][] solve(double[][] rhs){
        int size = this.l.length;
        double[][] y = new double[size][size];
        double[][] x = new double[size][size];

        for (int row = 0; row < size; row++) {
            for (int colum = 0; colum < size; colum++) {
                double summation = 0;
                for (int i = 0; i < row; i++)
                    summation += this.l[row][i] * y[i][colum];
                y[row][colum] = (rhs[row][colum] - summation);
            }
        }

        for (int row = size - 1; row >= 0; row--) {
            for (int colum = size - 1; colum >= 0; colum--) {
                double summation = 0;
                for (int i = size-1; i > row; i--)
                    summation += this.u[row][i] * x[i][colum];
                x[row][colum] = (y[row][colum] - summation) / u[row][row];
            }
        }

        return x;
    }

    public double[][] inverse(){
        return solve(identity(l.length));
    }

    public static double[][] identity(int size){
        double[][] id = new double[size][size];

        for (int i = 0; i < size; i++)
            id[i][i] = 1;

        return id;
    }
    public String stringOfL(){
        return stringOf(l);
    }

    public String stringOfU(){
        return stringOf(u);
    }

    private String stringOf(double[][] matrix){
        int[] aliment = new int[matrix.length];
        for (int column = 0; column < matrix.length; column++) {
            int size = 0;
            for (double[] doubles : matrix) {
                int length = String.valueOf(doubles[column]).length();
                if (length > size)
                    size = length;
            }
            aliment[column] = size;
        }

        String leftAlignFormat;
        String line;
        {
            StringBuilder alimentBuilder = new StringBuilder();
            StringBuilder lineBuilder = new StringBuilder();

            for (int i = 0; i < matrix.length; i++) {
                alimentBuilder.append("| %-").append(aliment[i]).append("s ");
                lineBuilder.append("+").append("-".repeat(aliment[i]+2));
            }
            alimentBuilder.append("|\n");
            lineBuilder.append("+\n");
            leftAlignFormat = alimentBuilder.toString();
            line = lineBuilder.toString();
        }

        StringBuilder builder = new StringBuilder();
        builder.append(line);
        for (double[] row : matrix) {
            builder.append(String.format(leftAlignFormat, Arrays.stream(row).boxed().toArray()));
            builder.append(line);
        }

        return builder.toString();
    }
}
