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
    double plane_I_V(double voltage);

    /**
     * The conductance plane.<br>
     * The derivative of current in respect to voltage is the <I>conductance</I>.
     * @param voltage the voltage.
     * @return the correspondent conductance at point voltage.
     */
    double plane_dI_dV(double voltage);
}
