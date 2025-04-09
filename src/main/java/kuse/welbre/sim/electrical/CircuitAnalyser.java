package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class CircuitAnalyser {
    public final boolean isNonLinear;
    public final boolean isDynamic;
    public final boolean isOperational;
    public final int nodes;
    public final int matrixSize;
    public final List<Circuit.Pin> pins;
    HashMap<Class<? extends Element>,List<Element>> map = new HashMap<>();

    public <T extends Element> List<T> get(Class<T> tClass){
        List<Element> elements = map.get(tClass);
        if (elements == null)
            return new ArrayList<>();
        else
            return (List<T>) elements;
    }

    /**
     * Find all nodes in the circuit.
     * A 2-length array that, the 0 addresses is the count of nodes in the circuit, the 1 is the number of independent voltage sources.
     */
    public CircuitAnalyser(Circuit circuit) {
        boolean isNonLinear = false, isDynamic = false, isOperational = false;
        int matrixSize = 0;
        pins = new ArrayList<>();

        for (Element element : circuit.getElements()) {
            for (Circuit.Pin pin : element.getPins())
                addPin(pins, pin);

            map.putIfAbsent(element.getClass(), new ArrayList<>());
            map.get(element.getClass()).add(element);

            if (element instanceof NonLinear)
                isNonLinear = true;
            if (element instanceof Dynamic)
                isDynamic = true;
            if (element instanceof Operational)
                isOperational = true;

            //Each of these terms contributes to matrix size
            //The node with an unknown voltage, and voltage sources with unknown currents.
            if (element instanceof RHSElement)
                matrixSize++;
            else if (element instanceof MultipleRHSElement rhs)
                matrixSize += rhs.getRHSAmount();
        }

        this.nodes = pins.size();
        this.matrixSize = matrixSize + this.nodes;
        this.isNonLinear = isNonLinear;
        this.isDynamic = isDynamic;
        this.isOperational = isOperational;
    }
    //add only if non-contains and isn't null.
    private void addPin(List<Circuit.Pin> pins, Circuit.Pin pin){
        if (pin == null)
            return;
        if (!pins.contains(pin))
            pins.add(pin);
    }
}
