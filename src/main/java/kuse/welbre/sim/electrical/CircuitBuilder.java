package kuse.welbre.sim.electrical;

import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

///A simples way to create larges circuits.
public class CircuitBuilder {
    public static CircuitBuilder BUILDER = null;

    public final List<Element> elements = new ArrayList<>();
    private final List<Watcher<?>> watchers = new ArrayList<>();
    public Circuit c = new Circuit();

    public CircuitBuilder() {
        BUILDER = this;
    }

    public <T extends Element> void addWatcher(T e, Consumer<T> cos){
        watchers.add(new Watcher<>(e,cos));
    }

    public Element.Pin pin(){
        return new Element.Pin();
    }

    public Circuit close(){
        BUILDER = null;
        c.addElement(elements);
        c.addWatcher(watchers.toArray(Watcher[]::new));

        return c;
    }
}
