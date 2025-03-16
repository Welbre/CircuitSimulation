package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.tools.MatrixBuilder;

/**
 * Any element that the LFS matrix or RHS depends on X.
 * Used to simulate diodes, variable resistence, switch, or non-linear resistence.
 */
public interface NonLinear {
    /**
     * Stamp the current-voltage plane.
     */
    void stamp_I_V(MatrixBuilder builder);

    /**
     * Stamp the conductance plane.<br>
     * The derivative of current in respect to voltage is the <I>conductance</I>.
     */
    void stamp_dI_dV(MatrixBuilder builder);
}
