package kuse.welbre.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LU {
    public final double[][] lu;//storage the lower triangle and the up triangle in one matrix.
    public final int[] swaps;

    private LU(double[][] lu, int[] swaps) {
        this.lu = lu;
        this.swaps = swaps;
    }

    ///Decompose lu matrix to a lu instance, the matrix lu at first param is lost in the process.
    public static LU decompose(double[][] lu){
        final int length = lu.length;

        final int[] swaps = new int[lu.length];//start swaps
        for (int i = 0; i < swaps.length; i++)
            swaps[i] = i ;

        for (int k = 0; k < length; k++) {//p_idx
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
                    swap(lu,swaps, k,biggest_row);
                }
            }

            final double p = lu[k][k];
            for (int j = k+1; j < length; j++) {//p_idx factor calculation.
                final double factor = lu[j][k] / p;
                if (factor == 0) continue;

                lu[j][k] = factor;
                for (int i = k+1; i < length; i++) {//apply the factor to p_idx.
                    lu[j][i] -= lu[k][i] * factor;
                }
            }
        }

        return new LU(lu, swaps);
    }

    private static <T> void swap(T[] a,int[] swaps, int row0, int row1){
        T swap = a[row0];
        a[row0] = a[row1];
        a[row1] = swap;
        final int t = swaps[row0];
        swaps[row0] = swaps[row1];
        swaps[row1] = t;
    }

    public double[] solve(double[] rhs){
        int size = this.lu.length;

        double[] r = new double[size];

        //Solve LY = rhs to find y
        for (int row = 0; row < size; row++) {
            double summation = 0;
            for (int colum = 0; colum < row; colum++)
                summation += this.lu[row][colum] * r[colum];
            r[row] = (rhs[swaps[row]] - summation);
        }

        //Solve UX=Y to find r
        for (int row = size-1; row >= 0; row--) {
            double summation = 0;
            for (int colum = size-1; colum > row; colum--)
                summation += this.lu[row][colum] * r[colum];
            r[row] = (r[row] - summation) / this.lu[row][row];
        }

        return r;
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
}
