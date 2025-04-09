package kuse.welbre.sim;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.exemples.Circuits;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class SerializationTest {
    public static Consumer<Element> getIfFails(Circuit expected, Circuit value){
        return broken -> {
            System.out.println("-".repeat(48));
            System.out.println("-".repeat(20) + "Expected" + "-".repeat(20));
            System.out.println("-".repeat(48));
            Main.printAllElements(expected);
            System.out.println("-".repeat(43));
            System.out.println("-".repeat(20) + "got" + "-".repeat(20));
            System.out.println("-".repeat(43));
            Main.printAllElements(value);
            System.out.println("-".repeat(44));
            System.out.println("-".repeat(20) + "fail" + "-".repeat(20));
            System.out.println("-".repeat(44));
            System.err.println(broken.toString());
        };
    }

    public static void checkIfCircuitIsEqual(Circuit expected, Circuit value){
        Element[] expected_elements = expected.getElements();
        Element[] value_elements = value.getElements();
        double[][] data_expected = new double[expected.getElements().length][3];
        for (int i = 0; i < data_expected.length; i++) {
            Element e = expected_elements[i];
            data_expected[i] = new double[]{e.getVoltageDifference(),e.getCurrent(),e.getPower()};
        }

        CircuitTest.testElements(value_elements, data_expected,getIfFails(expected,value));
    }

    @Test
    void serializeCircuit() throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Circuit circuit0 = Circuits.Capacitors.getRcCircuit();
        circuit0.preCompile();
        circuit0.tick();
        Circuit circuit1 = new Circuit();

        for (Element element : circuit0.getElements()) {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            var out = new DataOutputStream(st);
            Element newed = element.getClass().getConstructor().newInstance();

            element.serialize(out);
            newed.unSerialize(new DataInputStream(new ByteArrayInputStream(st.toByteArray())));
            st.close();
            circuit1.addElement(newed);
        }

        circuit1.preCompile();

        checkIfCircuitIsEqual(circuit0, circuit1);

        Main.printAllElements(circuit0);
        System.out.println("-".repeat(30));
        Main.printAllElements(circuit1);
    }
}
