package kuse.welbre.tools;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
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

    private record pivot_finder(int colum, int str, int ends, double[][] lu, int[] p_index, double[] p_pivot) implements Callable<Void> {
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
                final double factor = lu[i][p_idx] / pivot;
                if (factor == 0) continue;
                final double[] i_row = lu[i];
                final double[] p_row = lu[p_idx];
                lu[i][p_idx] = factor;//add the factor to lu matrix.
                for (int j = p_idx+1; j < lu.length; j++)//multiplication factor in row.
                    i_row[j] -= p_row[j] * factor;
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

    public static int threads = 6;
    public static ExecutorService executor =  Executors.newFixedThreadPool(threads);
    public static LU decompose(double[][] lu){
        if (threads == 1)
            return decomposeSingleThread(lu);

        try {
            return decomposeMultithread(executor,threads,lu);
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

    public static double[][] getArray(int size){
        double[][] matrix = new double[size][1];
        for (int i = 0; i < size; i++)
            matrix[i][0] = Math.random() % 10;

        return matrix;
    }

    public static double[][] getMatrix(int size){
        double[][] matrix = new double[size][size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = random.nextInt(-100,100);
            }
        }
        return matrix;
    }

    public static void algorithm(double[][] arr,int size, List<Callable<Void>> tasks, List<int[]> swaps, int idx[]) throws InterruptedException {
        int blockLen = (size - 1) / threads;
        int th_used = blockLen == 0 ? 1 : threads;

        double[][] p_pivot = new double[th_used][1];
        int[][] p_idx = new int[th_used][1];

        for (int n = 0; n < th_used; n++)
            tasks.add(new pivot_finder(
                    0,
                    n * blockLen,
                    n == th_used -1 ? size : (n+1) * blockLen,
                    arr,
                    p_idx[n],
                    p_pivot[n]
            ));

        executor.invokeAll(tasks);
        tasks.clear();

        double pivot = p_pivot[0][0];
        int pivot_idx = p_idx[0][0];
        for (int n = 1; n < th_used; n++)
            if (p_pivot[n][0] > pivot) {
                pivot = p_pivot[n][0];
                pivot_idx = p_idx[n][0];
            }

        if (pivot == 0)
            System.out.println("singular!");
        idx[0] = pivot_idx;
    }

    public static void plotFindPivot(boolean skip) throws Exception {
        if (!skip) {
            FileWriter writer = new FileWriter("./findPivotTime.csv");
            List<List<Number>> points = new ArrayList<>();
            for (int i = 0; i < 7; i++) points.add(new ArrayList<>());//populate
            for (int size = 10; size <= 20000; size+=50) points.getFirst().add(size);//populate size

            for (threads = 1; threads <= 6; threads++) {
                executor = Executors.newFixedThreadPool(threads);
                for (int size = 10; size <= 20000; size+=50) {//from 2 to 2024
                    double avg = 0;
                    for (int j = 0; j < 120; j++) {
                        double[][] arr = getArray(size);
                        List<Callable<Void>> tasks = new ArrayList<>();
                        List<int[]> swaps = new ArrayList<>();

                        int idx[] = new int[1];

                        long t0,t1;
                        t0 = System.nanoTime();
                        if (threads == 1){//pivot
                            new pivot_finder(0,0,size,arr,idx, new double[1]).call();
                            t1 = System.nanoTime();
                        } else {
                            algorithm(arr,size,tasks, swaps, idx);
                            t1 = System.nanoTime();
                            //Check if the result of multithread is equal to single thread.
                            int multi_idx = idx[0];
                            new pivot_finder(0,0,size,arr, idx, new double[1]).call();
                            if (multi_idx != idx[0]) {
                                executor.shutdown();
                                throw new Exception("Code is broken!!!");
                            }
                        }

                        avg += (t1 - t0) * 1e-6;

                        System.out.printf("\rthread(%d/%d) size(%d/%d) avg(%d/%d)", threads, 6, size, 20000, j, 120);
                    }
                    avg /= 120;
                    points.get(threads).add(avg);
                }
                executor.shutdown();
            }
            writer.write("size,1_threads,2_threads,3_threads,4_threads,5_threads,6_threads,\n");//header
            for (int i = 0; i < points.getFirst().size(); i++) {
                for (int j = 0; j < points.size(); j++) {
                    writer.append(points.get(j).get(i).toString()).append(',');
                }
                writer.append("\n");
            }
            writer.close();
        }
        Thread.sleep(200);
        Process process = Runtime.getRuntime().exec(new String[]{"py", "./Charts/Main.py", "./findPivotTime.csv"});
        process.waitFor();
        System.err.append(new String(process.getErrorStream().readAllBytes())).append("\n");
    }

    public static void plotSolverByThread(boolean skip) throws Exception {
        if (!skip) {
            FileWriter writer = new FileWriter("./timePerSize.csv");

            writer.write("size,1_threads,2_threads,3_threads,4_threads,5_threads,6_threads,\n");//header

            for (int i = 1; i <= 19; i++) {//from 2 to 2024
                int size = (int) Math.floor(Math.pow(1.5, i) + 0.5f);
                writer.write("%d,".formatted(size));

                for (threads = 1; threads <= 6; threads++) {
                    executor = Executors.newFixedThreadPool(threads);
                    double avg = 0;
                    for (int j = 0; j < 10; j++) {
                        double[][] matrix = getMatrix(size);
                        long t0 = System.nanoTime();
                        LU.decompose(matrix);
                        long t1 = System.nanoTime();
                        avg += (t1 - t0) * 1e-6;
                        System.out.printf("\rsize(%d/%d) thread(%d/%d) avg(%d/%d)", i, 19, threads, 6, j, 10);
                    }
                    executor.shutdown();
                    avg /= 10;
                    writer.write("%f,".formatted(avg));
                }
                writer.write("\n");
            }

            writer.close();
        }
        Thread.sleep(200);
        Process process = Runtime.getRuntime().exec(new String[]{"py", "./Charts/Main.py", "./timePerSize.csv"});
        process.waitFor();
        System.err.append(new String(process.getErrorStream().readAllBytes())).append("\n");
    }

    public static void main(String[] args) throws Exception {
        plotSolverByThread(true);
        //plotFindPivot(false);
    }
}
