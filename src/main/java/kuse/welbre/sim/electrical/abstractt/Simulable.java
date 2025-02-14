package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.tools.MatrixBuilder;

public interface Simulable {
    void initiate(Circuit circuit);

    void preEvaluation(MatrixBuilder builder);
    void posEvaluation(MatrixBuilder builder);
}
