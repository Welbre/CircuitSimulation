package kuse.welbre.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LU {
    public final double[][] lu;//storage the lower triangle and the up triangle in one matrix.
    public final int[][] swaps;

    private LU(double[][] lu, List<int[]> swaps) {
        this.lu = lu;
        this.swaps = new int[swaps.size()][2];
        for (int i = 0; i < swaps.size(); i++)
            this.swaps[i] = swaps.get(i);
    }

    //todo https://youtu.be/E3cCRcdFGmE
    public static LU decompose(double[][] lu){
        final int length = lu.length;
        final List<int[]> swaps = new ArrayList<>();

        for (int k = 0; k < length; k++) {//row
            {//pivot
                double b_pivot = Math.abs(lu[k][k]);
                int biggest_row = k;
                for (int j = k; j < length; j++) {
                    final double abs = Math.abs(lu[j][k]);
                    if (abs > b_pivot) {
                        b_pivot = abs;
                        biggest_row = j;
                    }
                }
                if (b_pivot == 0)
                    throw new IllegalStateException("Matrix is singular, can be decomposed!");
                if (biggest_row != k) { //needs swap
                    swap(lu,k,biggest_row);
                    swaps.add(new int[]{k,biggest_row});
                }
            }

            final double p = lu[k][k];
            for (int j = k+1; j < length; j++) {//column factor calculation.
                final double factor = lu[j][k] / p;
                if (factor == 0) continue;

                lu[j][k] = factor;
                for (int i = k+1; i < length; i++) {//apply the factor to row.
                    lu[j][i] -= lu[k][i] * factor;
                }
            }
        }

        return new LU(lu, swaps);
    }

    private static void swap(double[] a, int row0, int row1){
        double swap = a[row0];
        a[row0] = a[row1];
        a[row1] = swap;
    }

    private static <T> void swap(T[] a, int row0, int row1){
        T swap = a[row0];
        a[row0] = a[row1];
        a[row1] = swap;
    }

    public double[] solve(double[] rhs){
        int size = this.lu.length;

        double[] y = new double[size];
        double[] x = new double[size];

        //apply swap to rhs
        for (int[] swap : swaps)
            swap(rhs,swap[0],swap[1]);

        //Solve LY = rhs to find y
        for (int row = 0; row < size; row++) {
            double summation = 0;
            for (int colum = 0; colum < row; colum++)
                summation += this.lu[row][colum] * y[colum];
            y[row] = (rhs[row] - summation);
        }

        //Solve UX=Y to find x
        for (int row = size-1; row >= 0; row--) {
            double summation = 0;
            for (int colum = size-1; colum > row; colum--)
                summation += this.lu[row][colum] * x[colum];
            x[row] = (y[row] - summation) / this.lu[row][row];
        }

        return x;
    }

    public double[][] solve(double[][] rhs){
        int size = this.lu.length;
        double[][] y = new double[size][size];
        double[][] x = new double[size][size];

        for (int row = 0; row < size; row++) {
            for (int colum = 0; colum < size; colum++) {
                double summation = 0;
                for (int i = 0; i < row; i++)
                    summation += this.lu[row][i] * y[i][colum];
                y[row][colum] = (rhs[row][colum] - summation);
            }
        }

        for (int row = size - 1; row >= 0; row--) {
            for (int colum = size - 1; colum >= 0; colum--) {
                double summation = 0;
                for (int i = size-1; i > row; i--)
                    summation += this.lu[row][i] * x[i][colum];
                x[row][colum] = (y[row][colum] - summation) / lu[row][row];
            }
        }

        return x;
    }

    public double[][] inverse(){
        return solve(identity(lu.length));
    }

    public static double[][] identity(int size){
        double[][] id = new double[size][size];

        for (int i = 0; i < size; i++)
            id[i][i] = 1;

        return id;
    }

    @Override
    public String toString() {
        return stringOf(lu);
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

    public static void main(String[] args) {
        double[][] matrix = new double[][]{{7,5,3},{2,1,6},{1,2,3}};
        LU decompose = LU.decompose(matrix);
        System.out.println(decompose);
        System.out.println(Arrays.toString(decompose.solve(new double[]{1,2,3})));
    }
}
