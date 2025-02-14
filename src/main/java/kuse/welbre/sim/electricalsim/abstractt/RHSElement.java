package kuse.welbre.sim.electricalsim.abstractt;

import kuse.welbre.sim.electricalsim.tools.CircuitAnalyser;

public interface RHSElement {
    /**
     * In {@link kuse.welbre.sim.electricalsim.Circuit#preparePinsAndSources(CircuitAnalyser, double[][]) preparePinsAndSources} method this function will be called
     * to set the internal address of each element that contributes to the RightSideMatrix (known values).<br>
     * Ex: A voltage source that contributes to RHS with a voltage.
     */
    void setAddress(short address);

    ///@return the address set by {@link RHSElement#setAddress(short)}.
    int getAddress();

    /**
     * This function set an array with 1 length in the element.<br>
     * This array is a pointer to where the unknown value is stored.<br>
     * Ex: A voltage source contributes to a voltage in RHS, and needed a value in unknown matrix, in the case the current through the source.
     */
    void setValuePointer(double[] pointer);
}
