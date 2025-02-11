package kuse.welbre.sim;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

import kuse.welbre.sim.electricalsim.*;
import kuse.welbre.sim.electricalsim.exemples.Circuits;
import kuse.welbre.sim.electricalsim.tools.Tools;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

class CircuitTest {

    public static <T> String createExpectedXReceivedMsg(String propriety,T expected, T received){
        return String.format("%s is different! expected %s, got %s.", propriety, expected.toString(), received.toString());
    }

    public static boolean equals(double a, double b) {
        if (b == 0)
            return abs(a - b) < 0.100; //100m of absolute error.
        return (1 - abs(a/b)) < 0.01 && abs(a - b) < 0.100; //1% of error.
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////Resistence Tests/////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void testCircuit0(){
        double[][] answers = {{32, -4, -128}, {20,1, 20}, {8, 4, 32}, {4, 1, 4}, {24, 3, 72}};
        Circuit circuit = Circuits.Resistors.getCircuit0();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }

    @Test
    void testCircuit1(){
        double[][] answers = {{32, -10.6, -339.2}, {11.2, -3, -33.6}, {11.2, 5.6, 62.72}, {32, 8, 256}, {20.8, 2.6, 54.08}};
        Circuit circuit = Circuits.Resistors.getCircuit1();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }
    @Test
    void testCircuit2(){
        double[][] answers = {{40, -5, -200}, {20, -1, -20}, {10, 5, 50}, {18, 2, 36}, {8, 1, 8}, {30, 3, 90}, {12, 3, 36}};
        Circuit circuit = Circuits.Resistors.getCircuit2();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }
    @Test
    void testCircuit3(){
        double[][] answers = {{30, -3.474, -104.211}, {12.632, -2, -25.263}, {17.368, 3.474, 60.332}, {12.632, 4.211, 53.186}, {12.632, 1.263, 15.956}};
        Circuit circuit = Circuits.Resistors.getCircuit3();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }
    @Test
    void testCircuit4(){
        double[][] answers = {{10, -4.071, -40.714}, {15, -2.607, -39.107}, {-4.143, -2, 8.286}, {4.143, 2.071, 8.582}, {5.857, 1.464, 8.577}, {20.857, 2.607, 54.378}};
        Circuit circuit = Circuits.Resistors.getCircuit4();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }

    @Test
    void testCircuit5(){
        double[][] answers = {{10, -0.01, -0.1}, {10, -0.01, -0.1}, {10, 0.02, 0.2}};
        Circuit circuit = Circuits.Resistors.getCircuit5();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////Dynamic Tests/////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Nested
    class DynamicTest {
        private static final class DynamicData {
            final double[][] initialResultExpected;
            final double[][] finalResultExpected;
            private double time = 0;
            private int tick = 0;
            private double tickRate = Circuit.DEFAULT_TIME_STEP;
            final double total_time;
            private final Supplier<Circuit> circuitProvider;

            public DynamicData(Supplier<Circuit> circuitProvider, double[][] initialResultExpected, double[][] finalResultExpected, double totalTimeToSimulate) {
                this.initialResultExpected = initialResultExpected;
                this.finalResultExpected = finalResultExpected;
                total_time = totalTimeToSimulate;
                this.circuitProvider = circuitProvider;
            }

            public Consumer<Element> getDynamicFails(Circuit circuit, double modifiedTick){
                return element -> {
                    System.out.printf("Tick(%d)\t TickRate(%s)\t %s\n", tick,Tools.proprietyToSi(modifiedTick, "Hz"), Tools.proprietyToSi(time, "s"));
                    Main.printCircuitMatrix(circuit);
                    Main.printAllElements(circuit);
                    System.out.println();
                    circuit.exportToSpiceNetlist(System.out);
                    System.err.println(element);
                };
            }

            void test(){
                final double modified_tick_rate = tickRate;
                final Circuit circuit = circuitProvider.get();
                time = 0;
                tick = 0;

                circuit.setTickRate(modified_tick_rate);
                circuit.preCompile();

                Element[] elements = circuit.getElements();

                testElements(elements, initialResultExpected, getDynamicFails(circuit, modified_tick_rate));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                {
                    PrintStream printStream = new PrintStream(stream);
                    Main.printAllElements(circuit, printStream);
                    printStream.print("\n------------------------------------------------------\n");
                }

                //Simulate
                while (time < this.total_time) {
                    circuit.tick(modified_tick_rate);

                    this.tick++;
                    time += modified_tick_rate;
                }
                testElements(elements, finalResultExpected, getDynamicFails(circuit, modified_tick_rate));
                System.out.printf("Tick(%d)\t TickRate(%s)\t %s initial result:\n", 0,Tools.proprietyToSi(modified_tick_rate, "Hz"), Tools.proprietyToSi(0, "s"));
                System.out.println(stream);
                System.out.printf("Tick(%d)\t TickRate(%s)\t %s final result:\n", tick,Tools.proprietyToSi(modified_tick_rate, "Hz"), Tools.proprietyToSi(time, "s"));
                Main.printAllElements(circuit);
            }

            DynamicData setTickRate(double rate){
                tickRate = rate;
                return this;
            }
        }

        @Test
        @Order(1)
        void testRcCircuit(){
            final double[][] initialState = {{10, -10, -100}, {10, 10, 100}, {0, 10 ,0}};
            final double[][] finalState = {{10, 0, 0}, {0, 0, 0}, {10, 0, 0}};
            new DynamicData(Circuits.Capacitors::getRcCircuit, initialState, finalState, 0.6).test();
        }

        @Test
        @Order(1)
        void testCapacitorAssociationCircuit(){
            var expectInitial = new double[][]{{36,-3, -108}, {36,-3,-108}, {0,0,0}, {0,3,0}, {0,3,0}, {0,3,0}, {0,0,0}, {0,0,0}, {0,0,0}};
            var expectFinal = new double[][]{{36,0,0}, {0,0,0}, {0,0,0}, {2.1,0,0}, {2.1,0,0}, {31.79,0,0}, {4.2,0,0}, {4.2,0,0}, {4.2,0,0}};
            new DynamicData(Circuits.Capacitors::getAssociationCircuit, expectInitial, expectFinal, 2).test();
        }

        @Test
        @Order(2)
        void testMultiplesCapacitorsCircuit(){
            var expectInitial = new double[][]{{12,-0.345,-4.1379}, {-16,-32,-512}, {1.655, 0.13759, 0.228}, {1.655,0.207367,0.34401},{10.349,0.344958, 3.57},{15.992,31.984,512}, {0,0.3448,0}, {0,-31.789,0}, {0,0.137,0}};
            var expectFinal = new double[][]{{12, -0.10114, -1.2136}, {-16, 0.12526, 2}, {-0.264, 0.0022, 0}, {0.9854, -0.12318, -0.1213},{-3.034,-0.10114,0.3},{0.00626,0.12526,0},{23.91,0.101,2.41},{-15.93,0.002,0.031},{-14.68,-0.022,0.3229}};
            new DynamicData(Circuits.Capacitors::getMultiplesCapacitorsCircuit, expectInitial, expectFinal, 60).test();
        }

        @Test
        @Order(3)
        void testRLCircuit(){
            var expectInitial = new double[][]{{10,0,0}, {0,0,0}, {10,0,0}};
            var expectFinal = new double[][]{{10,-10,-100}, {10,-10,-100}, {0,10,0}};
            new DynamicData(Circuits.Inductors::getRlCircuit, expectInitial, expectFinal, 60).test();
        }

        @Test
        @Order(4)
        void testInductorAssociationCircuit(){
            var expectInitial = new double[][]{{36,0,0}, {0,0,0}, {0,0,0}, {0.12,0,0}, {0.12,0,0}, {35.76,0,0}, {0.2355,0,0}, {0.2355,0,0}, {0.2355,0,0}};
            var expectFinal = new double[][]{{36,-3, -108}, {36,-3,-108}, {0,0,0}, {0,3,0}, {0,3,0}, {0,3,0}, {0,0.00015,0}, {0,0.00015,0}, {0,0.00015,0}};//done
            new DynamicData(Circuits.Inductors::getAssociationCircuit, expectInitial, expectFinal, 1).test();
        }

        @Test
        @Order(5)
        void testInductorCircuit2(){
            var expectInitial = new double[][]{{12,0,0},{0,0,0},{0,0,0},{12,0,0}};
            var expectFinal = new double[][]{{12,0,0},{0,0,0},{12,0,0},{0,0,0}};//done
            new DynamicData(Circuits.RLC::getSeries, expectInitial, expectFinal, 1).test();
        }
    }
}