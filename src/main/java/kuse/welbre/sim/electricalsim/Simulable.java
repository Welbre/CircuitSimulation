package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

public interface Simulable {
    void tick(double dt, MatrixBuilder builder);

    void doInitialTick(MatrixBuilder builder);
}
