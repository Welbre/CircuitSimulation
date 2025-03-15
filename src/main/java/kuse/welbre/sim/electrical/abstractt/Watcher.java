package kuse.welbre.sim.electrical.abstractt;

import java.util.function.Consumer;

/**
 * This class is used to add a way to do checks and operation in a component at each time step.
 */
public class Watcher implements Runnable {
    private final Element element;
    private final Consumer<Element> consumer;

    public Watcher(Element element, Consumer<Element> consumer) {
        this.element = element;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        consumer.accept(element);
    }
}
