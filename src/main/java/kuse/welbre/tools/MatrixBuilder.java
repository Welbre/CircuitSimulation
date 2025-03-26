package kuse.welbre.tools;

import kuse.welbre.sim.electrical.CircuitAnalyser;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Element.Pin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatrixBuilder extends StaticBuilder {
    private final List<int[]> pairs = new ArrayList<>();
    private double[][] overload;
    private double[] RHS;
    protected LU lu;

    private boolean isDirt = true;

    public MatrixBuilder(CircuitAnalyser analyser) {
        super(analyser);
        RHS = new double[analyser.matrixSize];
    }

    public void stampVoltageSource(Pin a, Pin b, int sor_idx, double voltage) {
        stampVoltageSource(a,b,sor_idx);
        RHS[sor_idx] = voltage;
    }

    public void stampCurrentSource(Element.Pin a, Element.Pin b, double current){
        if (a != null)
            RHS[a.address] += current;
        if (b != null)
            RHS[b.address] -= current;
    }

    @Override
    public void stampConductance(Pin a, Pin b, double conductance) {
        if (isClosed)
        {
            if (a != null) {
                overload[a.address][a.address] += conductance;
                pairs.add(new int[]{a.address, a.address});
                if (b != null) {
                    overload[a.address][b.address] -= conductance;
                    overload[b.address][a.address] -= conductance;
                    pairs.add(new int[]{a.address, b.address});
                    pairs.add(new int[]{b.address, a.address});
                }
            }
            if (b != null) {
                overload[b.address][b.address] += conductance;
                pairs.add(new int[]{b.address, b.address});
            }
            dirt();
        }
        else
            super.stampConductance(a, b, conductance);
    }

    public void stampRHS(int idx, double value){
        if (isClosed) throw new IllegalStateException("Try stamp left hand side in a closed builder!");
        RHS[idx] = value;
    }

    @Override
    public void close() {
        super.close();
        overload = Tools.deepCopy(LHS);
        clear();
    }

    /**
     * @return The equation system result, using {@link LU}.
     */
    public double[] getResult(){
        if (!isClosed)
            throw new IllegalStateException("Try get result in a non closed matrix builder!");
        if (isDirt)
            clear();
        return lu.solve(RHS);
    }

    public double[] getRHS() {
        return RHS;
    }

    public double[] getCopyOfRHS() {
        return Tools.deepCopy(RHS);
    }

    public void clearZMatrix(){
        Arrays.fill(RHS, 0);
    }


    private void dirt(){
        isDirt = true;
    }

    private void clear(){
        lu = LU.decompose(overload);
        for (int[] pair : pairs)
            overload[pair[0]][pair[1]] = LHS[pair[0]][pair[1]];

        isDirt = false;
    }
}
