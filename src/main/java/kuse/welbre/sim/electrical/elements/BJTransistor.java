package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Element3Pin;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;

import static java.lang.Math.exp;

/**
 * A BJT, the pin A is the collector, the B is the base, and C is the emissor.<br>
 * The current is from A and B to C.
 */
public class BJTransistor extends Element3Pin implements NonLinear {
    public static final double DEFAULT_SATURATION = 1e-6;
    private double beta_for = 100.0;
    private double beta_rev = 33.0;
    //--------------------------------------Forward-----------------------------------
    private double current_f;
    private double sat_f = DEFAULT_SATURATION;
    private double n_f = 1;
    private double temp_f = 0.025852;
    private double den_f = n_f * temp_f;
    //-----------------------------------Backward------------------------------------
    private double current_r;
    private double sat_r = DEFAULT_SATURATION;
    private double n_r = 1;
    private double temp_r = 0.025852;
    private double den_r = n_r * temp_r;

    public BJTransistor() {
    }

    public BJTransistor(Pin collector, Pin base, Pin emissor) {
        super(collector, base, emissor);
    }

    public BJTransistor(double beta_forward) {
        setBeta(beta_forward);
    }

    public BJTransistor(Pin pinA, Pin pinB, Pin pinC, double beta_forward) {
        super(pinA, pinB, pinC);
        setBeta(beta_forward);
    }


    @Override
    public double[] getProperties() {
        return new double[]{beta_for, beta_rev};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"",""};
    }

    /// returns the current in the emissor pin.
    @Override
    public double getCurrent() {
        return current_r - beta_for*current_f;
    }

    @Override
    public void stamp_I_V(MatrixBuilder builder) {
        current_f = -sat_f*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinA())/den_f) - 1);
        current_r = -sat_r*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinC())/den_r) - 1);

        builder.stampCurrentSource(getPinA(), getPinB(), current_f - beta_rev*current_r);//from base to collector.
        builder.stampCurrentSource(getPinC(), getPinB(), current_r - beta_for*current_f);//from base to emissor.
    }

    @Override
    public void stamp_dI_dV(MatrixBuilder builder) {
        final double gf = -sat_f*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinA()) / den_f))/den_f;
        final double gr = -sat_r*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinC()) / den_r))/den_r;

        final Pin a = getPinA();
        final Pin b = getPinB();
        final Pin c = getPinC();
        if (c != null)
            builder.stampLHS(c.address,c.address, gr);
        if (a != null)
            builder.stampLHS(a.address,a.address, gf);
        if (b != null)
            builder.stampLHS(b.address,b.address, gf -beta_rev*gr +gr -beta_for*gf);
        if (c != null && a != null) {
            builder.stampLHS(c.address, a.address, -beta_for*gf);
            builder.stampLHS(a.address, c.address, -beta_rev*gr);
        }
        if (c != null && b != null){
            builder.stampLHS(c.address, b.address, -gr + beta_for*gf);
            builder.stampLHS(b.address, c.address, beta_rev*gr - gr);
        }
        if (b != null && a != null) {
            builder.stampLHS(b.address,a.address, -gf + beta_for*gf);
            builder.stampLHS(a.address,b.address, -gf +beta_rev*gr);
        }
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        stamp_dI_dV(builder);
        stamp_I_V(builder);
    }

    public double getBeta() {
        return beta_for;
    }

    public void setBeta(double beta) {
        if (beta == 0)
            throw new IllegalArgumentException("Beta can't be zero!");
        this.beta_for = beta;
        this.beta_rev = beta / 3.0;
    }
}
