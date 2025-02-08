package kuse.welbre.sim.electricalsim;

import kuse.welbre.sim.electricalsim.tools.MatrixBuilder;

public interface Simulable {
    void initiate(Circuit circuit);

    void preEvaluation(MatrixBuilder builder);
    void posEvaluation(MatrixBuilder builder);
}
