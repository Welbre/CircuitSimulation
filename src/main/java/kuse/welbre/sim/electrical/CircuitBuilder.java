package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;

import java.util.ArrayList;
import java.util.List;

///A simples way to create larges circuits.
public class CircuitBuilder {
    public static CircuitBuilder BUILDER = null;

    public List<Element> elements = new ArrayList<>();

    public CircuitBuilder() {
        BUILDER = this;
    }

    public Element.Pin pin(){
        return new Element.Pin();
    }

    public Circuit close(){
        BUILDER = null;
        Circuit c = new Circuit();
        c.addElement(elements);

        return c;
    }
}
