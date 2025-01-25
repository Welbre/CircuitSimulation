package kuse.welbre.ctf;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

import kuse.welbre.ctf.electricalsim.Circuit;
import kuse.welbre.ctf.electricalsim.Element;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class TestCircuit {
    @Test
    void TestCircuitAnswers() {
        double[][][] answers = getExpectedAnswers();
        Circuit[] circuits = getCircuits();

        for (int i = 0; i < circuits.length; i++) {
            Circuit circuit = circuits[i];
            circuit.tick(1);

            Element[] elements = circuit.getElements();
            for (int j = 0; j < elements.length; j++) {
                Element element = elements[j];
                try {
                    testElement(element, answers[i][j]);
                } catch (AssertionFailedError err) {
                    Main.printCircuitMatrix(circuit);
                    Main.printAllComponents(circuit);
                    System.err.println(element);
                    throw err;
                }

            }
            System.out.println("Passed!");
        }
    }

    @Nested
    class TestCircuitAnswers {
        @Test
        void otherTest(){

        }
    }

    Circuit[] getCircuits(){
        return new Circuit[]{
                Main.loadCase1(), Main.loadCase2(),
                Main.loadCase3(), Main.loadCase4(),
                Main.loadCase5(), Main.loadCase6(),
                Main.capacitorTest()
        };
    }

    double[][][] getExpectedAnswers(){
        return new double[][][]{
                {{32, -4, -128}, {20,1, 20}, {8, 4, 32}, {4, 1, 4}, {24, 3, 72}},
                {{32, -10.6, -339.2}, {11.2, -3, -33.6}, {11.2, 5.6, 62.72}, {32, 8, 256}, {20.8, 2.6, 54.08}},
                {{40, -5, -200}, {20, -1, -20}, {10, 5, 50}, {18, 2, 36}, {8, 1, 8}, {30, 3, 90}, {12, 3, 36}},
                {{30, -3.474, -104.211}, {12.632, -2, -25.263}, {17.368, 3.474, 60.332}, {12.632, 4.211, 53.186}, {12.632, 1.263, 15.956}},
                {{10, -4.071, -40.714}, {15, -2.607, -39.107}, {-4.143, -2, 8.286}, {4.143, 2.071, 8.582}, {5.857, 1.464, 8.577}, {20.857, 2.607, 54.378}},
                {{10, -0.01, -0.1}, {10, -0.01, -0.1}, {10, 0.02, 0.2}},
                {{10, 0, 0}, {0, 0, 0}, {10, 0 ,0}}
        };
    }

    void testElement(Element element, double[] expected){
        assertTrue(equals(abs(element.getVoltageDifference()), abs(expected[0])), "Voltage is different!" + String.format(" expected %f, got %f.", abs(expected[0]), abs(element.getVoltageDifference())));
        assertTrue(equals(abs(element.getCurrent()), abs(expected[1])), "Current is different!" + String.format(" expected %f, got %f.", abs(expected[1]), abs(element.getCurrent())));
        assertTrue(equals(element.getPower(), expected[2]), "Power is different!" + String.format(" expected %f, got %f.", expected[2], element.getPower()));
    }

    boolean equals(double a, double b) {
        return abs(a - b) < 0.001;
    }
}