package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.elements.ACVoltageSource;
import kuse.welbre.sim.electrical.elements.Capacitor;
import kuse.welbre.tools.MatrixBuilder;

/**
 * An element that isn't constant in time domain.<br>
 * Like {@link ACVoltageSource ACVoltageSource} that the voltage changes in time.<br>
 * or a {@link Capacitor capacitor} that the voltage depends on the derivative of current in respect of time.
 */
public interface Dynamic {
    ///This method is called to prepare the element to start the dynamics.
    void initiate(Circuit circuit);
    ///This method is called before the evolution step occurs.<br><i>At this point stamp can be performed in RHS matrix</i>
    void preEvaluation(MatrixBuilder builder);

    /**
     * This method is called after the evolution step occurs.<br><i>At this point, stamp in RHS matrix will be vanished and not applied to the next evolution.</i><br>
     * Use this method only to compute non-direct-related changes voltage, current, or resistence.<br>
     * Exemple in a {@link ACVoltageSource ACVoltageSource} a field {@link ACVoltageSource#theta theta} is update in this method,
     * this filed defines where in the sine wave the voltage source is, so theta is related to the voltage, but don't modify the voltage it is self.
     * Therefore, only in {@link ACVoltageSource#preEvaluation(MatrixBuilder) ACVoltageSource#preEvaluation} the voltage is stamped in RHS matrix.
     */
    void posEvaluation(MatrixBuilder builder);

    /**
     * Defines the minimal time step to this component operate correctly.
     * @return the stime step
     */
    default double getMinTickRate(){
        return Double.MAX_VALUE;
    }
}
