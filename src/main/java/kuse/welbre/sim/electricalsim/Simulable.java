package kuse.welbre.sim.electricalsim;

public interface Simulable {
    void tick(double dt, Circuit circuit);

    void doInitialTick(Circuit circuit);
}
