package kuse.welbre.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Tools {
    public static double norm(double[] a){
        double sum = 0.0;
        for (double x : a)
            sum += x * x;

        return Math.sqrt(sum);
    }

    public static double[] multiply(double[] a, double b){
        double[] c = new double[a.length];

        for (int i = 0; i < a.length; i++)
            c[i] = a[i] * b;

        return c;
    }

    public static double[] multiply(double[][] a, double[] b){
        final int n = a.length;
        double[] c = new double[n];
        double sum;

        for (int i = 0; i < n; i++) {
            sum = 0;
            for (int j = 0; j < n; j++) {
                sum += a[i][j] * b[j];
            }
            c[i] = sum;
        }

        return c;
    }

    public static double[] add(double[] a,double b){
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++)
            result[i] = a[i] + b;
        return  result;
    }

    public static double[] subtract(double[] a,double b){
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++)
            result[i] = a[i] - b;
        return  result;
    }

    public static double[] subtract(double[] a, double[] b){
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++)
            result[i] = a[i] - b[i];
        return  result;
    }

    /**
     * @see <a href="https://www.sanfoundry.com/java-program-find-inverse-matrix/#google_vignette">Sanfoundry</a>
     */
    public static double[][] invert(double[][] a)
    {
        int n = a.length;
        double[][] x = new double[n][n];
        double[][] b = new double[n][n];
        int[] index = new int[n];
        for (int i=0; i<n; ++i)
            b[i][i] = 1;

        // Transform the matrix into an upper triangle
        final int swaps  = gaussian(a, index);
        // Calculate determinant
        double determinant = 1.0;
        for (int i = 0; i < n; i++) {
            determinant *= a[index[i]][i];
        }
        determinant *= (swaps % 2 == 0) ? 1 : -1;
        //Check if the matrix is invertible
        if (determinant == 0 || !Double.isFinite(determinant))
            throw new IllegalStateException("Matrix determinant("+ determinant + ") invalid!, some fault happen in stamp process.");

        // Update the matrix b[i][j] with the ratios stored
        for (int i=0; i<n-1; ++i)
            for (int j=i+1; j<n; ++j)
                for (int k=0; k<n; ++k)
                    b[index[j]][k]
                            -= a[index[j]][i]*b[index[i]][k];

        // Perform backward substitutions
        for (int i=0; i<n; ++i)
        {
            x[n-1][i] = b[index[n-1]][i]/a[index[n-1]][n-1];
            for (int j=n-2; j>=0; --j)
            {
                x[j][i] = b[index[j]][i];
                for (int k=j+1; k<n; ++k)
                {
                    x[j][i] -= a[index[j]][k]*x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }

    // Method to carry out the partial-pivoting Gaussian
    // elimination.  Here index[] stores pivoting order.
    private static int gaussian(double[][] a, int[] index) {
        int n = index.length;
        double[] c = new double[n];
        int swaps = 0;

        // Initialize the index
        for (int i=0; i<n; ++i)
            index[i] = i;

        // Find the rescaling factors, one from each row
        for (int i=0; i<n; ++i)
        {
            double c1 = 0;
            for (int j=0; j<n; ++j)
            {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }

        // Search the pivoting element from each column
        int k = 0;
        for (int j=0; j<n-1; ++j)
        {
            double pi1 = 0;
            for (int i=j; i<n; ++i)
            {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1)
                {
                    pi1 = pi0;
                    k = i;
                }
            }

            if (k != j) { // Row swap
                int itmp = index[j];
                index[j] = index[k];
                index[k] = itmp;
                swaps++;
            }

            // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i=j+1; i<n; ++i)
            {
                double pj = a[index[i]][j]/a[index[j]][j];

                // Record pivoting ratios below the diagonal
                a[index[i]][j] = pj;

                // Modify other elements accordingly
                for (int l=j+1; l<n; ++l)
                    a[index[i]][l] -= pj*a[index[j]][l];
            }
        }

        return swaps;
    }

    public static double[] deepCopy(double[] original){
        int n0 = original.length;
        double[] copy = new double[n0];
        System.arraycopy(original, 0, copy, 0, n0);

        return copy;
    }

    public static double[][] deepCopy(double[][] original){
        int n0 = original.length, n1 = original[0].length;
        double[][] copy = new double[n0][n1];
        for (int i = 0; i < n0; i++)
            System.arraycopy(original[i], 0, copy[i], 0, n1);

        return copy;
    }

    public static String proprietyToSi(double value, final String unity) {
        return proprietyToSi(value, unity, 2);
    }

    public static String proprietyToSi(double[] value, final String[] unity, int precision){
        var b = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            b.append(proprietyToSi(value[i], unity[i], precision)).append(',');
        }
        b.deleteCharAt(b.length()-1);
        return b.toString();
    }

    public static String proprietyToSi(double value, final String unity, int precision){
        var abs = Math.abs(value);
        String prefix = ""; double mult = 1;

        if (abs > 1E15) return String.format("%s%s%s", BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros(), prefix, unity);
        else if (abs > 1E12) {prefix = "T";mult = 1E-12;}
        else if (abs >= 1E9) {prefix = "G"; mult = 1E-9;}
        else if (abs >= 1E6) {prefix = "M"; mult = 1E-6;}
        else if (abs >= 1E3) {prefix = "k"; mult = 1E-3;}
        else if (abs < 1E3 && abs >= 1) {prefix = "";}
        else if (abs >= 1E-3) {prefix = "m"; mult = 1E3;}
        else if (abs >= 1E-6) {prefix = "μ"; mult = 1E6;}
        else if (abs >= 1E-9) {prefix = "n"; mult = 1E9;}
        else return String.format("%s%s%s", BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros(), prefix, unity);

        return String.format("%s%s%s", BigDecimal.valueOf(value * mult).setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString(), prefix, unity);
    }
}