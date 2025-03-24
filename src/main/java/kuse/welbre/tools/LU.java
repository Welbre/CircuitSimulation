package kuse.welbre.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LU {
    public final double[][] lu;//storage the lower triangle and the up triangle in one matrix.
    public final int[][] swaps;

    private LU(double[][] lu, List<int[]> swaps) {
        this.lu = lu;
        this.swaps = new int[swaps.size()][2];
        for (int i = 0; i < swaps.size(); i++)
            this.swaps[i] = swaps.get(i);
    }

    private record pivot_executor(int colum, int str, int ends, double[][] lu, int[] p_index, double[] p_pivot) implements Callable<Void> {
        @Override
        public Void call(){
            double pivot = Math.abs(lu[str][colum]);
            int pivot_idx = str;
            for (int i = str + 1; i < ends; i++) {
                final double abs = Math.abs(lu[i][colum]);
                if (abs > pivot) {
                    pivot = abs;
                    pivot_idx = i;
                }
            }

            p_index[0] = pivot_idx;
            p_pivot[0] = pivot;
            return null;
        }
    }
    private record row_multiplication(int start, int end, int p_idx, double pivot, double[][] lu) implements Callable<Void> {
        @Override
        public Void call(){
            for (int i = start; i < end; i++) {//block index
                final double[] i_row = lu[i];
                final double[] p_row = lu[p_idx];
                final double factor = i_row[p_idx] / pivot;
                lu[i][p_idx] = factor;//add the factor to lu matrix.
                for (int j = p_idx+1; j < lu.length; j++)//multiplication factor in row.
                    i_row[j] -= p_row[j]*factor;
            }
            return null;
        }
    }

    public static LU decomposeMultithread(ExecutorService executor,int th_amount, double[][] lu) throws InterruptedException {
        List<Callable<Void>> tasks = new ArrayList<>();
        final int length = lu.length;
        final List<int[]> swaps = new ArrayList<>();

        for (int k = 0; k < length; k++) {//p_idx
            {//pivot
                int blockLen = (length - k) / th_amount;
                int th_used = blockLen == 0 ? 1 : th_amount;
                double[][] p_pivot = new double[th_used][1];
                int[][] p_idx = new int[th_used][1];

                for (int i = 0; i < th_used; i++)
                    tasks.add(new pivot_executor(
                            k,
                            blockLen * i + k,
                            i == th_used-1 ? length : ((i+1) * blockLen) + k,
                            lu,
                            p_idx[i],
                            p_pivot[i])
                    );

                executor.invokeAll(tasks);
                tasks.clear();

                double pivot = p_pivot[0][0];
                int pivot_idx = p_idx[0][0];
                for (int i = 1; i < th_used; i++)
                    if (p_pivot[i][0] > pivot) {
                        pivot = p_pivot[i][0];
                        pivot_idx = p_idx[i][0];
                    }

                if (pivot == 0)
                    throw new IllegalStateException("Matrix is singular, can be decomposed!");
                if (pivot_idx != k) { //needs swap
                    swap(lu,k,pivot_idx);
                    swaps.add(new int[]{k,pivot_idx});
                }
            }
            if (length-k-1 > 0) {//check if need's multiplication.
                //multiplication a p_idx by a factor.
                int blockLen = (length - k - 1) / th_amount;
                int th_used = blockLen == 0 ? 1 : th_amount;

                final double p = lu[k][k];
                for (int i = 0; i < th_used; i++) {
                    tasks.add(new row_multiplication(
                            i * blockLen + k + 1,
                            (th_used - 1) == i ? length : (i + 1) * blockLen + k + 1,
                            k, p, lu
                    ));
                }
                executor.invokeAll(tasks);
                tasks.clear();
            }
        }
        return new LU(lu, swaps);
    }

    public static LU decomposeSingleThread(double[][] lu){
        final int length = lu.length;
        final List<int[]> swaps = new ArrayList<>();

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
                    swap(lu,k,biggest_row);
                    swaps.add(new int[]{k,biggest_row});
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

    public static LU decompose(double[][] lu){
        try {
            return decomposeMultithread(Executors.newFixedThreadPool(6),6,lu);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    public static double[][] getMatrix(int size){
        double[][] matrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = Math.random() % 10;
            }
        }
        return matrix;
    }

    public static double average(double[] times){
        double sum = 0;
        for (double time : times) {
            sum += time;
        }
        return sum / times.length;
    }

    public static void main(String[] args) throws InterruptedException {
        {
            int size = 2000;
            int avg = 50;
            double[][] original = getMatrix(size);
            {
                double[] times = new double[avg];
                for (int i = 0; i < avg; i++) {
                    double[][] matrix = Tools.deepCopy(original);
                    final long t0 = System.nanoTime();
                    LU lu_single = LU.decomposeSingleThread(matrix);
                    final long t1 = System.nanoTime();
                    times[i] = (t1 - t0) * 1e-6;
                }
                System.out.println("Time single thread %f ms".formatted(average(times)));
            }
            for (int i = 1; i <= 6; i++) {
                ExecutorService service = Executors.newFixedThreadPool(i);
                double[] times = new double[avg];

                for (int j = 0; j < avg; j++) {
                    double[][] matrix = Tools.deepCopy(original);

                    long t0 = System.nanoTime();
                    LU lu_multi = LU.decomposeMultithread(service,i,matrix);
                    long t1 = System.nanoTime();

                    times[j] = (t1 - t0) * 1e-6;
                }
                System.out.println("Time multiThread(%d) %f ms".formatted(i,average(times)));

                service.shutdown();
            }

            //System.out.println("Single thread \n" + lu_single);
            System.out.println("/".repeat(50).concat("\n").repeat(3));
            //System.out.println("Multi thread \n" + lu_multi);
        }
    }
}
