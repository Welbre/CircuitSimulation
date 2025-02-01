package kuse.welbre.sim;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

import kuse.welbre.sim.electricalsim.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.function.Consumer;

class CircuitTest {

    public static <T> String createExpectedXReceivedMsg(String propriety,T expected, T received){
        return String.format("%s is different! expected %s, got %s.", propriety, expected.toString(), received.toString());
    }

    public static boolean equals(double a, double b) {
        return abs(a - b) < 0.1;
    }

    public static Consumer<Element> getIfFails(Circuit circuit){
        return element -> {
            Main.printCircuitMatrix(circuit);
            Main.printAllElements(circuit);
            System.err.println(element);
        };
    }

    public static void testElement(Element element, double[] expected){
        assertTrue(equals(abs(element.getVoltageDifference()), abs(expected[0])), createExpectedXReceivedMsg("Voltage", abs(expected[0]), abs(element.getVoltageDifference())));
        assertTrue(equals(abs(element.getCurrent()), abs(expected[1])), createExpectedXReceivedMsg("Current", abs(expected[1]), abs(element.getCurrent())));
        assertTrue(equals(abs(element.getPower()), abs(expected[2])), createExpectedXReceivedMsg("Power", abs(expected[2]), abs(element.getPower())));
    }

    public static void testElements(Element[] elements,double[][] answers, Consumer<Element> ifFails){
        Assumptions.assumeTrue(elements.length == answers.length, "Fail in test, the number of elements and answers is different!");

        for (int i = 0; i < elements.length; i++) {
            Element element = elements[i];
            try {
                testElement(element, answers[i]);
            } catch (AssertionFailedError err) {
                ifFails.accept(element);
                throw err;
            }
        }
    }

    public static void testCircuit(int addr) {
        Circuit circuit = getCircuits()[addr];
        double[][] answers = getExpectedAnswers()[addr];
        circuit.preCompile();
        testElements(circuit.getElements(), answers, getIfFails(circuit));
    }

    @Test
    void testCircuit1(){
        testCircuit(0);
    }

    @Test
    void testCircuit2(){
        testCircuit(1);
    }
    @Test
    void testCircuit3(){
        testCircuit(2);
    }
    @Test
    void testCircuit4(){
        testCircuit(3);
    }
    @Test
    void testCircuit5(){
        testCircuit(4);
    }

    @Test
    void testCircuit6(){
        testCircuit(5);
    }

    @Nested
    class DynamicTest {
        private static abstract class DynamicData {
            final double[][] initialResultExpected;
            final double[][] finalResultExpected;
            int tick = 0;
            final int total_steps_to_simulate;

            public DynamicData(double[][] initialResultExpected, double[][] finalResultExpected, int totalStepsToSimulate) {
                this.initialResultExpected = initialResultExpected;
                this.finalResultExpected = finalResultExpected;
                total_steps_to_simulate = totalStepsToSimulate;
            }

            abstract Circuit createCircuit();

            public Consumer<Element> getDynamicFails(Circuit circuit){
                return element -> {
                    System.out.printf("Tick(%d)\t %.1fms\n", tick, tick * Circuit.TIME_STEP * 1000);
                    Main.printCircuitMatrix(circuit);
                    Main.printAllElements(circuit);
                    System.err.println(element);
                };
            }

            void test(){
                Circuit circuit = createCircuit();
                circuit.preCompile();
                Element[] elements = circuit.getElements();
                testElements(elements, initialResultExpected, getDynamicFails(circuit));
                //Simulate
                while (tick < this.total_steps_to_simulate) {
                    circuit.tick(Circuit.TIME_STEP);
                    this.tick++;
                }
                testElements(elements, finalResultExpected, getDynamicFails(circuit));
            }
        }

        @Test
        void testCapacitorResistanceCircuit(){
            var cap = new DynamicData(
                    new double[][]{{10, -10, -100}, {10, -10, -100}, {0, 0 ,0}},
                    new double[][]{{10, 0, 0}, {0, 0, 0}, {10, 0, 0}},
                    120
            ){
                @Override
                Circuit createCircuit() {
                    return Main.capacitorTest();
                }
            };
            cap.test();
        }

        @Test
        void testCapacitorCircuit1(){
            var expectInitial = new double[][]{{12,-3,-36}, {-12,-3,36}, {0,0,0}, {0,0,0}, {0,0,0}, {0,0,0}};
            var expectFinal = new double[][]{{12,0,0}, {0,0,0}, {0,0,0}, {6,0,0}, {6,0,0}, {12,0,0}};
            var cap = new DynamicData(expectInitial, expectFinal, 120){
                @Override
                Circuit createCircuit() {
                    Circuit circuit = new Circuit();
                    var v = new VoltageSource(12);
                    var r1 = new Resistor(4);
                    var r2 = new Resistor(2);
                    var c1 = new Capacitor(0.5 / 1000);
                    var c2 = new Capacitor(0.5 / 1000);
                    var c3 = new Capacitor(0.001);

                    circuit.addElement(v, r1, r2, c1,c2,c3);

                    v.connect(r1.getPinA(), null);
                    Element.Pin pb = r1.getPinB();
                    c1.connectA(pb);
                    c2.connect(c1.getPinB(), null);
                    r2.connect(pb, c3.getPinA());
                    c3.connectB(null);

                    return circuit;
                }
            };
            cap.test();
        }

        @Test
        void testCapacitorCircuit2(){
            var expectInitial = new double[][]{{12,-0.345,-4.1379}, {-16,-32,-512}, {1.655, 0.13759, 0.228}, {1.655,0.207367, 0.34401}, {10.349,0.344958, 3.57}, {15.992,31.984,512}, {0,0,0}, {0,0,0}, {0,0,0}};
            var expectFinal = new double[][]{{12,-0.574,-6.891}, {-16,1.094,-17.5}, {5.137,0.428102,2.199}, {8.017,1,8.034}, {17.22,0.574015,9.885}, {0.544513,1.089,0.592988}, {2.218,0.574015,1.273}, {15.455,0.0869,1.343}, {2.301, 0.428102,0.9852}};
            var cap = new DynamicData(expectInitial, expectFinal, 600){
                @Override
                Circuit createCircuit() {
                    return Main.compilicatedCapacitorCircuit();
                }
            };
            cap.test();
        }
        @Test
        void testInductorResistanceCircuit(){
            var expectInitial = new double[][]{{10,0,0}, {0,0,0}, {10,0,0}};
            var expectFinal = new double[][]{{10,-10,-100}, {10,-10,-100}, {0,10,0}};
            var cap = new DynamicData(expectInitial, expectFinal, 600){
                @Override
                Circuit createCircuit() {
                    return Main.getInductorResistanceCircuit();
                }
            };
            cap.test();
        }
    }


    public static Circuit[] getCircuits(){
        return new Circuit[]{
                Main.loadCase1(), Main.loadCase2(),
                Main.loadCase3(), Main.loadCase4(),
                Main.loadCase5(), Main.loadCase6(),
                Main.capacitorTest()
        };
    }

    public static double[][][] getExpectedAnswers(){
        return new double[][][]{
                {{32, -4, -128}, {20,1, 20}, {8, 4, 32}, {4, 1, 4}, {24, 3, 72}},
                {{32, -10.6, -339.2}, {11.2, -3, -33.6}, {11.2, 5.6, 62.72}, {32, 8, 256}, {20.8, 2.6, 54.08}},
                {{40, -5, -200}, {20, -1, -20}, {10, 5, 50}, {18, 2, 36}, {8, 1, 8}, {30, 3, 90}, {12, 3, 36}},
                {{30, -3.474, -104.211}, {12.632, -2, -25.263}, {17.368, 3.474, 60.332}, {12.632, 4.211, 53.186}, {12.632, 1.263, 15.956}},
                {{10, -4.071, -40.714}, {15, -2.607, -39.107}, {-4.143, -2, 8.286}, {4.143, 2.071, 8.582}, {5.857, 1.464, 8.577}, {20.857, 2.607, 54.378}},
                {{10, -0.01, -0.1}, {10, -0.01, -0.1}, {10, 0.02, 0.2}}
        };
    }
}