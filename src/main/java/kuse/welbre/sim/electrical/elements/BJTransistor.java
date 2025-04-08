package kuse.welbre.sim.electrical.elements;

import kuse.welbre.sim.electrical.abstractt.Element3Pin;
import kuse.welbre.sim.electrical.abstractt.NonLinear;
import kuse.welbre.tools.MatrixBuilder;
import kuse.welbre.tools.Tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.Math.*;

/**
 * A BJT, the pin A is the collector, the B is the base, and C is the emissor.<br>
 * The current is from A and B to C.
 */
public class BJTransistor extends Element3Pin implements NonLinear {
    public enum TYPE {NPN, PNP}
    public static final double DEFAULT_SATURATION = 1e-6;

    private double alpha_for = 0.8;
    private double alpha_rev = 0.8/20;
    private TYPE type = TYPE.NPN;
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

    public BJTransistor(TYPE type, double beta) {
        this.type = type;
        setBeta(beta);
    }

    public BJTransistor(Pin pinA, Pin pinB, Pin pinC, double beta) {
        super(pinA, pinB, pinC);
        setBeta(beta);
    }

    public BJTransistor(Pin pinA, Pin pinB, Pin pinC, TYPE type, double beta) {
        super(pinA, pinB, pinC);
        setBeta(beta);
        this.type = type;
    }


    @Override
    public double[] getProperties() {
        return new double[]{alpha_for / (1-alpha_for)};
    }

    @Override
    public String[] getPropertiesSymbols() {
        return new String[]{""};
    }

    /// returns the current in the emissor pin.
    @Override
    public double getCurrent() {
        return -current_r + alpha_for *current_f;
    }

    @Override
    public double getVoltageDifference() {
        return GET_VOLTAGE_DIFF(getPinB(), getPinC());
    }

    @Override
    public void stamp_I_V(MatrixBuilder builder) {
        final Pin a = getPinA(),b = getPinB(), c = getPinC();
        if (type == TYPE.NPN) {
            current_r = min(sat_r * (exp(GET_VOLTAGE_DIFF(b, a) / den_r) - 1), 10e6);
            current_f = min(sat_f * (exp(GET_VOLTAGE_DIFF(b, c) / den_f) - 1), 10e6);

            builder.stampCurrentSource(b, a, -current_r + alpha_for * current_f);//from base to collector.
            builder.stampCurrentSource(b, c, -current_f + alpha_rev * current_r);//from base to emissor.
        } else {
            current_r = min(sat_r * (exp(GET_VOLTAGE_DIFF(a, b) / den_r) - 1), 10e6);
            current_f = min(sat_f * (exp(GET_VOLTAGE_DIFF(c, b) / den_f) - 1), 10e6);

            builder.stampCurrentSource(a, b, -current_r + alpha_for * current_f);//from collector to base.
            builder.stampCurrentSource(c, b, -current_f + alpha_rev * current_r);//from emissor to base.
        }
    }

    @Override
    public void stamp_dI_dV(MatrixBuilder builder) {
        final Pin a = getPinA(),b = getPinB(), c = getPinC();
        double gee,gec,gce,gcc;
        if (type == TYPE.NPN) {
            final double gf = sat_f * (exp(GET_VOLTAGE_DIFF(getPinB(), getPinC()) / den_f)) / den_f;//forward diode
            final double gr = sat_r * (exp(GET_VOLTAGE_DIFF(getPinB(), getPinA()) / den_r)) / den_r;//reverse diode

            gee = -gf;
            gec = alpha_rev * gr;
            gce = alpha_for * gf;
            gcc = -gr;
        } else {
            final double gf = sat_f * (exp(GET_VOLTAGE_DIFF(getPinC(), getPinB()) / den_f)) / den_f;//forward diode
            final double gr = sat_r * (exp(GET_VOLTAGE_DIFF(getPinA(), getPinB()) / den_r)) / den_r;//reverse diode

            gee = -gf;
            gec = alpha_rev * gr;
            gce = alpha_for * gf;
            gcc = -gr;
        }

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
        double ce = GET_VOLTAGE_DIFF(getPinA(), getPinC());
        double be = GET_VOLTAGE_DIFF(getPinB(), getPinC());
        double ic = -current_r + alpha_for*current_f;
        double ib = -current_f + alpha_rev*current_r +ic;
        return String.format(
                "%s(%s)[%s,%s,%s]: BE:%.2fv, BC:%.2fv, CE:%.2fv Ic%.2fA, Ib%.2fA, Pc:%.2fW Pb:%.2fW",
                this.getClass().getSimpleName(),
                Tools.proprietyToSi(getProperties(), getPropertiesSymbols(), 2),
                getPinA() == null ? "gnd" : getPinA().address+1,
                getPinB() == null ? "gnd" : getPinB().address+1,
                getPinC() == null ? "gnd" : getPinC().address+1,
                be,
                GET_VOLTAGE_DIFF(getPinB(),getPinA()),
                ce, ic, ib, ic*ce, ib*be
        );
    }

    @Override
    public void serialize(DataOutputStream stream) throws IOException {
        super.serialize(stream);
        stream.writeDouble(alpha_for);
        stream.writeByte(type.ordinal());
        stream.writeDouble(sat_f);
        stream.writeDouble(n_f);
        stream.writeDouble(temp_f);
        stream.writeDouble(sat_r);
        stream.writeDouble(n_r);
        stream.writeDouble(temp_r);
    }

    @Override
    public void unSerialize(DataInputStream buffer) throws IOException {
        super.unSerialize(buffer);
        alpha_for = buffer.readDouble();
        alpha_rev = alpha_for/20;
        type = TYPE.values()[buffer.readByte()];
        //--------------------------------------Forward-----------------------------------
        sat_f = buffer.readDouble();
        n_f = buffer.readDouble();
        temp_f = buffer.readDouble();
        den_f = n_f * temp_f;
        //-----------------------------------Backward------------------------------------
        sat_r = buffer.readDouble();
        n_r = buffer.readDouble();
        temp_r = buffer.readDouble();
        den_r = n_r * temp_r;
    }
}
