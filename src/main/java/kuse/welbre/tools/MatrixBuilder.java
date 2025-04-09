package kuse.welbre.tools;

import kuse.welbre.sim.electrical.CircuitAnalyser;
import kuse.welbre.sim.electrical.Circuit.Pin;

import java.util.ArrayList;
import java.util.List;

public class MatrixBuilder extends StaticBuilder {
    private final List<int[]> lhs_pairs = new ArrayList<>();
    private final List<Integer> rhs_pairs = new ArrayList<>();
    private final List<Integer> rhs_lock_pairs = new ArrayList<>();
    private double[][] lhs_overlap;

    private double[] rhs_overlap;
    private double[] rhs_lock;
    private final double[] rhs_static;
    protected LU lu;

    private boolean isLocked = false;

    public MatrixBuilder(CircuitAnalyser analyser) {
        super(analyser);
        rhs_static = new double[analyser.matrixSize];
    }

    public void stampVoltageSource(Pin a, Pin b, int sor_idx, double voltage) {
        stampVoltageSource(a,b,sor_idx);
        stampRHS(sor_idx, voltage);
    }

    public void stampCurrentSource(Pin a, Pin b, double current){
        if (isClosed) {
            if (a != null) {
                if (isLocked) {
                    rhs_lock[a.address] += current;
                    rhs_lock_pairs.add((int) a.address);
                }
                else {
                    rhs_overlap[a.address] += current;
                    rhs_pairs.add((int) a.address);
                }
            }
            if (b != null) {
                if (isLocked) {
                    rhs_lock[b.address] -= current;
                    rhs_lock_pairs.add((int) b.address);
                }
                else {
                    rhs_overlap[b.address] -= current;
                    rhs_pairs.add((int) b.address);
                }
            }
        }
        else {
            if (a != null)
                rhs_static[a.address] += current;
            if (b != null)
                rhs_static[b.address] -= current;
        }
    }

    @Override
    public void stampLHS(int row, int colum, double value) {
        if (isClosed) {
            lhs_overlap[row][colum] += value;
            lhs_pairs.add(new int[]{row,colum});
        } else
            super.stampLHS(row, colum, value);
    }

    @Override
    public void stampConductance(Pin a, Pin b, double conductance) {
        if (isClosed)
        {
            if (a != null) {
                lhs_overlap[a.address][a.address] += conductance;
                lhs_pairs.add(new int[]{a.address, a.address});
                if (b != null) {
                    lhs_overlap[a.address][b.address] -= conductance;
                    lhs_overlap[b.address][a.address] -= conductance;
                    lhs_pairs.add(new int[]{a.address, b.address});
                    lhs_pairs.add(new int[]{b.address, a.address});
                }
            }
            if (b != null) {
                lhs_overlap[b.address][b.address] += conductance;
                lhs_pairs.add(new int[]{b.address, b.address});
            }
        }
        else
            super.stampConductance(a, b, conductance);
    }

    public void stampRHS(int idx, double value){
        if (isClosed) {
            if (isLocked) {
                rhs_lock[idx] = value;
                rhs_lock_pairs.add(idx);
            }
            else {
                rhs_overlap[idx] = value;
                rhs_pairs.add(idx);
            }
        } else
            rhs_static[idx] = value;
    }

    @Override
    public void close() {
        super.close();
        lhs_overlap = Tools.deepCopy(lhs);
        rhs_overlap = Tools.deepCopy(rhs_static);
        clear();
    }

    /**
     * @return The equation system result, using {@link LU}.
     */
    public double[] getResult(){
        if (!isClosed)
            throw new IllegalStateException("Try get result in a non closed matrix builder!");

        double[] solution;
        if (rhs_pairs.isEmpty() && lhs_pairs.isEmpty()) {
            if (lu == null)
                lu = LU.decompose(Tools.deepCopy(lhs_overlap));
            solution = lu.solve(Tools.deepCopy(getRhs()));
        } else {
            lu = LU.decompose(Tools.deepCopy(lhs_overlap));
            //todo don't forget to change how the lu swaps,to don't affect the original rhs.
            solution = lu.solve(Tools.deepCopy(getRhs()));
            clear();
        }

        return solution;
    }

    public double[] getRhs() {
        return isLocked ? rhs_lock : rhs_overlap;
    }

    public double[] getCopyOfRHS() {
        return Tools.deepCopy(rhs_static);
    }

    @Override
    public double[][] getLhs() {
        return lhs_overlap;
    }

    public void lock(){
        rhs_lock = Tools.deepCopy(rhs_overlap);
        this.isLocked = true;
    }

    public void unlock(){
        rhs_lock = null;
        this.isLocked = false;
    }

    /// Clear all modification and returns the Matrix builder to the state before the close.
    public void clear(){
        clearLhs();
        clearRhs();
    }

    public void clearRhs(){
        if (isLocked) {
            for (int idx : rhs_lock_pairs)
                rhs_lock[idx] = rhs_overlap[idx];
            rhs_lock_pairs.clear();
        }
        else {
            for (int idx : rhs_pairs)
                rhs_overlap[idx] = rhs_static[idx];
            rhs_pairs.clear();
        }
    }

    public void clearLhs(){
        for (int[] pair : lhs_pairs)
            lhs_overlap[pair[0]][pair[1]] = lhs[pair[0]][pair[1]];

        lhs_pairs.clear();
    }
}
