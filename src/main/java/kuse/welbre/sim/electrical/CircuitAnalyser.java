package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.MultipleRHSElement;
import kuse.welbre.sim.electrical.abstractt.RHSElement;

import java.util.*;

public final class CircuitAnalyser {
    public final int nodes;
    public final int matrixSize;
    public final Set<Element.Pin> pins;
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
        int matrixSize = 0;
        pins = new HashSet<>();

        for (Element element : circuit.getElements()) {
            pins.add(element.getPinA());
            pins.add(element.getPinB());

            map.putIfAbsent(element.getClass(), new ArrayList<>());
            map.get(element.getClass()).add(element);

            //Each of these terms contributes to matrix size
            //The node with an unknown voltage, and voltage sources with unknown currents.
            if (element instanceof RHSElement)
                matrixSize++;
            else if (element instanceof MultipleRHSElement rhs)
                matrixSize += rhs.getRHSAmount();
        }

        //removes the ground from the list, because it isn't having relevance in the matrix.
        pins.remove(null);

        this.nodes = pins.size();
        this.matrixSize = matrixSize + this.nodes;
    }
}
