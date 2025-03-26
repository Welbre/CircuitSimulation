package kuse.welbre.sim.electrical.abstractt;

import java.util.function.Consumer;

/**
 * This class is used to add a way to do checks and operation in a component at each time step.
 */
public class Watcher<T extends Element> implements Runnable {
    private final T element;
    private final Consumer<T> consumer;

    public Watcher(T element, Consumer<T> consumer) {
        this.element = element;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        consumer.accept(element);
    }
}
