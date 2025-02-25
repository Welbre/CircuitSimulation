package kuse.welbre.sim.electrical.abstractt;

/**
 * Any element that the LFS matrix or RHS depends on X.
 * Used to simulate diodes, variable resistence, switch, or non-linear resistence.
 */
public interface NonLinear {
    /**
     * The current-voltage plane.
     * @param voltage the voltage.
     * @return the correspondent current at point voltage.
     */
    double I_V_Plane(double voltage);

    /**
     * The derivative current-voltage plane.<br>
     * The derivative of current in voltage is the <I>conductance</I>.
     * @param voltage the voltage.
     * @return the correspondent conductance at point voltage.
     */
    double dI_dV_Plane(double voltage);
}
