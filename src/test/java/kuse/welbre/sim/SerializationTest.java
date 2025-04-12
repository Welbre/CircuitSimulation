package kuse.welbre.sim;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.exemples.Circuits;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SerializationTest {
    public static Consumer<Element> getIfFails(Circuit expected, Circuit value){
        return broken -> {
            System.err.println("-".repeat(48));
            System.err.println("-".repeat(20) + "Expected" + "-".repeat(20));
            System.err.println("-".repeat(48));
            Main.printAllElements(expected,System.err);
            System.err.println("-".repeat(43));
            System.err.println("-".repeat(20) + "got" + "-".repeat(20));
            System.err.println("-".repeat(43));
            Main.printAllElements(value,System.err);
            System.err.println("-".repeat(44));
            System.err.println("-".repeat(20) + "fail" + "-".repeat(20));
            System.err.println("-".repeat(44));
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

    private static List<Method> getAllCircuits(){
        List<Method> list = new ArrayList<>();
        for (Class<?> aClass : Circuits.class.getDeclaredClasses()) {
            for (Method method : aClass.getMethods()) {
                if (method.getReturnType() == Circuit.class)
                    list.add(method);
            }
        }
        return list.stream().sorted(Comparator.comparing(Method::getName)).toList();
    }

    @Test
    void testAllCircuits() throws Exception{
        List<Method> circuits = getAllCircuits();

        for (Method constructor : circuits) {
            System.out.println("-".repeat(20) + "%s::%s".formatted(constructor.getDeclaringClass().getSimpleName(), constructor.getName()) + "-".repeat(20));
            Circuit circuit = (Circuit) constructor.invoke(null);
            Circuit copy;

            circuit.preCompile();
            for (int i = 0; i < 5000; i++) {
                copy = new Circuit();

                ByteArrayOutputStream st = new ByteArrayOutputStream();
                var out = new DataOutputStream(st);

                circuit.serialize(out);

                var in = new DataInputStream(new ByteArrayInputStream(st.toByteArray()));
                copy.unSerialize(in);

                out.close();
                in.close();

                checkIfCircuitIsEqual(circuit, copy);
                circuit.tick();
            }

            Main.printAllElements(circuit);
            System.out.println("-".repeat(50));
        }
    }

    @Test
    void serializeCircuit() throws IOException{
        Circuit circuit0 = Circuits.Inductors.getAssociationCircuit();
        Circuit circuit1 = null;

        circuit0.preCompile();
        for (int i = 0; i < 500; i++) {
            circuit0.tick();
            circuit1 = new Circuit();

            ByteArrayOutputStream st = new ByteArrayOutputStream();
            var out = new DataOutputStream(st);

            circuit0.serialize(out);
            circuit1.unSerialize(new DataInputStream(new ByteArrayInputStream(st.toByteArray())));
            st.close();

            checkIfCircuitIsEqual(circuit0, circuit1);
        }

        Main.printAllElements(circuit0);
        System.out.println("-".repeat(30));
        Main.printAllElements(circuit1);
    }
}
