package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element3Pin;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import static java.lang.Math.exp;
import static java.lang.Math.min;

/**
 * A BJT, the pin A is the collector, the B is the base, and C is the emissor.<br>
 * The current is from A and B to C.
 */
public class BJTransistor extends Element3Pin implements NonLinear {
    public static final double DEFAULT_SATURATION = 1e-6;
    private double alpha_for = 0.8;
    private double alpha_rev = 0.8/20;
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
        return new double[]{alpha_for, alpha_rev};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{"",""};
    }

    /// returns the current in the emissor pin.
    @Override
    public double getCurrent() {
        return current_r - alpha_for *current_f;
    }

    @Override
    public void stamp_I_V(MatrixBuilder builder) {
        current_r = sat_r*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinA())/den_r) - 1);
        current_f = sat_f*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinC())/den_f) - 1);

        builder.stampCurrentSource(getPinB(), getPinA(), -current_r + alpha_for *current_f);//from base to collector.
        builder.stampCurrentSource(getPinB(), getPinC(), -current_f + alpha_rev *current_r);//from base to emissor.
    }

    @Override
    public void stamp_dI_dV(MatrixBuilder builder) {
        final double gf = sat_f*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinC()) / den_f))/den_f;//forward diode
        final double gr = sat_r*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinA()) / den_r))/den_r;//reverse diode

        final double gee = -gf;
        final double gec = alpha_rev *gr;
        final double gce = alpha_for *gf;
        final double gcc = -gr;

        final Pin a = getPinA();
        final Pin b = getPinB();
        final Pin c = getPinC();
        if (c != null)//emissor
            builder.stampLHS(c.address,c.address, -gee);
        if (a != null)//collector
            builder.stampLHS(a.address,a.address, -gcc);
        if (b != null)//base
            builder.stampLHS(b.address,b.address, -gee-gec-gce-gcc);
        if (c != null && a != null) {//emissor collector
            builder.stampLHS(c.address, a.address, -gec);
            builder.stampLHS(a.address, c.address, -gce);
        }
        if (c != null && b != null){//base emissor
            builder.stampLHS(c.address, b.address, gee+gec);
            builder.stampLHS(b.address, c.address, gee+gce);
        }
        if (b != null && a != null) {//base collector
            builder.stampLHS(b.address,a.address, gec+gcc);
            builder.stampLHS(a.address,b.address, gce+gcc);
        }
    }

    @Override
    public void stamp(MatrixBuilder builder) {
        stamp_dI_dV(builder);
        stamp_I_V(builder);
    }

    public double getBeta() {
        return alpha_for;
    }

    public void setBeta(double beta) {
        if (beta == 0)
            throw new IllegalArgumentException("Beta can't be zero!");
        this.alpha_for = beta / (beta + 1);
        this.alpha_rev = this.alpha_for / 20.0;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s)[%s,%s,%s]: BE:%.2fv, BC:%.2fv, CE:%.2fv Ic%.2fA, Ib%.2fA, Pc:%.2fW Pb:%.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 2),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                GET_VOLTAGE_DIFF(getPinB(),getPinC()),
                GET_VOLTAGE_DIFF(getPinB(),getPinA()),
                GET_VOLTAGE_DIFF(getPinA(),getPinC()),
                -current_r + alpha_for *current_f,
                -current_f + alpha_rev *current_r,
                (-current_r + alpha_for *current_f)*GET_VOLTAGE_DIFF(getPinA(),getPinC()),
                (-current_f + alpha_rev *current_r)*GET_VOLTAGE_DIFF(getPinB(),getPinC())
        );
    }
}
