package kuse.welbre.sim;

import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.exemples.Circuits;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public class SerializationTest {
    @Test
    void serializeCircuit() throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Circuit circuit0 = Circuits.BJT.getNPNCircuit();
        Circuit circuit2 = new Circuit();

        for (Element element : circuit0.getElements()) {
            ByteArrayOutputStream st = new ByteArrayOutputStream();
            var out = new DataOutputStream(st);
            Element newed = element.getClass().getConstructor().newInstance();

            element.serialize(out);
            newed.unSerialize(new DataInputStream(new ByteArrayInputStream(st.toByteArray())));
            st.close();
            circuit2.addElement(newed);
        }

        circuit0.preCompile();
        circuit2.preCompile();

        Main.printAllElements(circuit0);
        System.out.println("-".repeat(30));
        Main.printAllElements(circuit2);
    }
}
