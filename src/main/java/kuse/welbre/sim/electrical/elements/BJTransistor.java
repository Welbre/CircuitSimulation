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
    private double beta_for = 100;
    private double beta_rev = beta_for / 3.0;
    //--------------------------------------Emissor-----------------------------------
    private double current_e;
    private double sat_e = DEFAULT_SATURATION;
    private double n_e = 1;
    private double temp_e = 0.025852;
    private double den_e = n_e * temp_e;
    //-----------------------------------Collector------------------------------------
    private double current_c;
    private double sat_c = DEFAULT_SATURATION;
    private double n_c = 1;
    private double temp_c = 0.025852;
    private double den_c = n_c * temp_c;

    public BJTransistor() {
    }

    public BJTransistor(Pin pinA, Pin pinB, Pin pinC) {
        super(pinA, pinB, pinC);
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
        return current_e + current_c;
    }

    @Override
    public void stamp_I_V(MatrixBuilder builder) {
        final double diode_c = Math.min(sat_c*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinA()) / den_c)-1),999999);
        final double diode_e = Math.min(sat_e*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinC()) / den_e)-1),999999);

        current_e = -diode_e + beta_rev*diode_c;
        current_c = beta_for*diode_e - diode_c;
        builder.stampCurrentSource(getPinB(), getPinA(), current_c);//from base to collector.
        builder.stampCurrentSource(getPinB(), getPinC(), current_e);//from base to emissor.
    }

    @Override
    public void stamp_dI_dV(MatrixBuilder builder) {//possivelment problema no stamp do transistor, algo parece errado.
        final double diode_c = Math.min(sat_c*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinA()) / den_c))/den_c, 99999);
        final double diode_e = Math.min(sat_e*(exp(GET_VOLTAGE_DIFF(getPinB(), getPinC()) / den_e))/den_e, 99999);
        final double gee = -(-diode_e);
        final double gec = -(beta_rev*diode_c);
        final double gce = -(beta_for*diode_e);
        final double gcc = -(-diode_c);
        final Pin a = getPinA();
        final Pin b = getPinB();
        final Pin c = getPinC();
        if (c != null)
            builder.stampLHS(c.address,c.address, -gee);
        if (a != null)
            builder.stampLHS(a.address,a.address, -gcc);
        if (b != null)
            builder.stampLHS(b.address,b.address, -gee-gec-gce-gcc);
        if (c != null && a != null) {
            builder.stampLHS(c.address, a.address, -gec);
            builder.stampLHS(a.address, c.address, -gce);
        }
        if (c != null && b != null){
            builder.stampLHS(c.address, b.address, gee+gec);
            builder.stampLHS(b.address, c.address, gee+gce);
        }
        if (b != null && a != null) {
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
        return beta_for;
    }

    public void setBeta(double beta) {
        if (beta == 0)
            throw new IllegalArgumentException("Beta can't be zero!");
        this.beta_for = beta;
        this.beta_rev = beta / 3.0;
    }
}
