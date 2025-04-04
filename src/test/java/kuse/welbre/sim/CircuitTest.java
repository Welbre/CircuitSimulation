package kuse.welbre.sim;

import kuse.welbre.sim.Tools.ElementOMeter;
import kuse.welbre.sim.electrical.Circuit;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.*;
import kuse.welbre.sim.electrical.exemples.Circuits;
import kuse.welbre.tools.Tools;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircuitTest {

    public static <T> String createExpectedXReceivedMsg(String propriety,T expected, T received){
        return String.format("%s is different! expected %s, got %s.", propriety, expected.toString(), received.toString());
    }

    public static boolean equals(double a, double b) {
        if (b == 0)
            return abs(a - b) < 0.100; //100m of absolute error.
        return abs((1 - abs(a/b))) < 0.01; //1% of error.
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
        if (expected.length >= 3)
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
    @Benchmark.benchmark
    void testCircuit0(){
        double[][] answers = {{32, -4, -128}, {20,1, 20}, {8, 4, 32}, {4, 1, 4}, {24, 3, 72}};
        Circuit circuit = Circuits.Resistors.getCircuit0();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }

    @Test
    @Benchmark.benchmark
    void testCircuit1(){
        double[][] answers = {{32, -10.6, -339.2}, {11.2, -3, -33.6}, {11.2, 5.6, 62.72}, {32, 8, 256}, {20.8, 2.6, 54.08}};
        Circuit circuit = Circuits.Resistors.getCircuit1();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }
    @Test
    @Benchmark.benchmark
    void testCircuit2(){
        double[][] answers = {{40, -5, -200}, {20, -1, -20}, {10, 5, 50}, {18, 2, 36}, {8, 1, 8}, {30, 3, 90}, {12, 3, 36}};
        Circuit circuit = Circuits.Resistors.getCircuit2();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }
    @Test
    @Benchmark.benchmark
    void testCircuit3(){
        double[][] answers = {{30, -3.474, -104.211}, {12.632, -2, -25.263}, {17.368, 3.474, 60.332}, {12.632, 4.211, 53.186}, {12.632, 1.263, 15.956}};
        Circuit circuit = Circuits.Resistors.getCircuit3();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }
    @Test
    @Benchmark.benchmark
    void testCircuit4(){
        double[][] answers = {{10, -4.071, -40.714}, {15, -2.607, -39.107}, {-4.143, -2, 8.286}, {4.143, 2.071, 8.582}, {5.857, 1.464, 8.577}, {20.857, 2.607, 54.378}};
        Circuit circuit = Circuits.Resistors.getCircuit4();
        circuit.preCompile();

        testElements(circuit.getElements(), answers, getIfFails(circuit));

        Main.printAllElements(circuit);
    }

    @Test
    @Benchmark.benchmark
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
    @Benchmark.benchmark
    @Order(1)
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

                //simulate
                circuit.tick(total_time);
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
        @Benchmark.benchmark
        @Order(1)
        void testRcCircuit(){
            final double[][] initialState = {{10, -10, -100}, {10, 10, 100}, {0, 10 ,0}};
            final double[][] finalState = {{10, 0, 0}, {0, 0, 0}, {10, 0, 0}};
            new DynamicData(Circuits.Capacitors::getRcCircuit, initialState, finalState, 0.6).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(1)
        void testCapacitorAssociationCircuit(){
            var expectInitial = new double[][]{{36,-3, -108}, {36,-3,-108}, {0,0,0}, {0,3,0}, {0,3,0}, {0,3,0}, {0,0,0}, {0,0,0}, {0,0,0}};
            var expectFinal = new double[][]{{36,0,0}, {0,0,0}, {0,0,0}, {2.1,0,0}, {2.1,0,0}, {31.79,0,0}, {4.2,0,0}, {4.2,0,0}, {4.2,0,0}};
            new DynamicData(Circuits.Capacitors::getAssociationCircuit, expectInitial, expectFinal, 2).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(2)
        void testMultiplesCapacitorsCircuit(){
            var expectInitial = new double[][]{{12,-0.345,-4.1379}, {-16,-32,-512}, {1.655, 0.13759, 0.228}, {1.655,0.207367,0.34401},{10.349,0.344958, 3.57},{15.992,31.984,512}, {0,0.3448,0}, {0,-31.789,0}, {0,0.137,0}};
            var expectFinal = new double[][]{{12, -0.10114, -1.2136}, {-16, 0.12526, 2}, {-0.264, 0.022, 0}, {0.9854, -0.12318, -0.1213},{-3.034,-0.10114,0.306},{0.0626,0.12526,0},{23.91,0.101,2.41},{-15.93,0.002085,0.03322},{-14.68,-0.022,0.3229}};
            new DynamicData(Circuits.Capacitors::getMultiplesCapacitorsCircuit, expectInitial, expectFinal, 60).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(3)
        void testRLCircuit(){
            var expectInitial = new double[][]{{10,0,0}, {0,0,0}, {10,0,0}};
            var expectFinal = new double[][]{{10,-10,-100}, {10,-10,-100}, {0,10,0}};
            new DynamicData(Circuits.Inductors::getRlCircuit, expectInitial, expectFinal, 60).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(4)
        void testInductorAssociationCircuit(){
            var expectInitial = new double[][]{{36,0,0}, {0,0,0}, {0,0,0}, {0.12,0,0}, {0.12,0,0}, {35.76,0,0}, {0.24,0,0}, {0.24,0,0}, {0.24,0,0}};
            var expectFinal = new double[][]{{36,-3, -108}, {36,-3,-108}, {0,0,0}, {0,3,0}, {0,3,0}, {0,3,0}, {0,0.000159,0}, {0,0.000159,0}, {0,0.000159,0}};
            new DynamicData(Circuits.Inductors::getAssociationCircuit, expectInitial, expectFinal, 1).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(5)
        void testSeriesRCLCircuit(){
            var expectInitial = new double[][]{{12,0,0},{0,0,0},{0,0,0},{12,0,0}};
            var expectFinal = new double[][]{{12,0,0},{0,0,0},{12,0,0},{0,0,0}};
            new DynamicData(Circuits.RLC::getSeries, expectInitial, expectFinal, 1).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(6)
        void testSeriesRCLOscillationFreeCircuit(){
            var expectInitial = new double[][]{{12,0,0},{0,0,0},{0,0,0},{12,0,0}};
            var expectFinal = new double[][]{{12,0,0},{0,0,0},{12,0,0},{0,0,0}};
            new DynamicData(Circuits.RLC::getSeriesNoOscillation, expectInitial, expectFinal, 0.38).test();
        }

        @Test
        @Benchmark.benchmark
        @Order(7)
        void testParallelRCLCircuit(){
            var expectInitial = new double[][]{{12,-12,-144},{12,12,144},{0,0,0},{0,12,0},{0,0,0}};
            var expectFinal = new double[][]{
                    {12,-12,-144}, {-11.9489,1.194891e+01,1.194891e+01*-11.9489},
                    {0,0,0}, {0,0,0},
                    {0,1.189983e+01,0}
            };
            new DynamicData(Circuits.RLC::getParallel, expectInitial, expectFinal, 3).setTickRate(0.05).test();
        }
    }

    @Nested
    @Benchmark.benchmark
    @Order(2)
    class DependentSources {
        @Test
        @Order(1)
        void testCCCSCircuit(){
            double[][] answers = {{10,-1,10},{10,1,10},{300,-3,-900},{300,-3,-900}};
            Circuit circuit = Circuits.CurrentControlledCurrentSources.getCCCSWithResistors();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }
        @Test
        @Order(2)
        void testVCVSCircuit(){
            double[][] answers = {{5,-1/3.0,-1.667}, {5/3.0,1/3.0,5.0/9.0},{10/3.0,1/3.0,10/9.0},{10,-1/5.0,-10/5.0},{10,1/5.0,2}};
            Circuit circuit = Circuits.CurrentControlledCurrentSources.getVCVSWithResistors();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }

        @Test
        @Order(3)
        void testCCVSCircuit(){
            double[][] answers = {{1e3,-1,-1e3},{1e3,1,1e3},{2,-4,-8},{2,4,8}};
            Circuit circuit = Circuits.CurrentControlledCurrentSources.getCCVSWithResistors();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }

        @Test
        @Order(4)
        void testVCCSCircuit(){
            double[][] answers = {{1e3,0,0},{0.5,-1,-0.5},{0.5,1,0.5}};
            Circuit circuit = Circuits.CurrentControlledCurrentSources.getVCCSWithResistors();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }
    }

    @Nested
    @Benchmark.benchmark
    @Order(3)
    class Diodes{
        @Test
        @Benchmark.benchmark
        @Order(1)
        void testDiodeResistence(){
            double[][] answers = new double[][]{{10,-9.58441,95.8441},{9.58441,9.58441,9.58441*9.58441},{0.41559,9.58441,0.41559*9.58441}};
            Circuit circuit = Circuits.Diodes.getDiodeResistence();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }
        @Test
        @Benchmark.benchmark
        @Order(2)
        void testReverseBiasDiode(){
            final double sat = 1e-6;
            double[][] answers = new double[][]{{-10,-sat,10*sat},{0,sat,0},{-10,sat,-sat*10}};
            Circuit circuit = Circuits.Diodes.getDiodeReverseBias();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }
        @Test
        @Benchmark.benchmark
        @Order(3)
        void testSeriesDiode(){
            double[][] answers = new double[][]{{10,0.928914,9.28914},{0.355431,0.928914,0.330},{0.355431,0.928914,0.330},{9.289,0.9289,8.629}};
            Circuit circuit = Circuits.Diodes.getSeriesDiode();
            circuit.preCompile();

            testElements(circuit.getElements(), answers, getIfFails(circuit));

            Main.printAllElements(circuit);
        }
        @Test
        @Benchmark.benchmark
        @Order(4)
        void testHalfWaveRectifier(){
            final double sat = 1e-3;//1mA
            final double fwd = 0.7;//700mV
            final double openI = 0.080;//80mv
            double[][] answersZero = new double[][]{{0,0,0},{0,0,0},{0,0,0}};
            double[][] answersRev = new double[][]{{-10,sat,10*sat},{500*1e-6,sat,sat*500*1e-6},{-10,sat,-10*sat}};
            double[][] answersFwd = new double[][]{{10,16.899,10*16.899},{8.449,16.899,8.449*16.899},{-1.551,16.899,-1.551*16.899}};
            Circuit circuit = new Circuit();

            VoltageSource v = new ACVoltageSource(-10, 1);
            Resistor r = new Resistor(0.5);
            Diode d = new Diode(fwd, openI, sat);
            circuit.addElement(v,r,d);
            v.connect(r.getPinA(), null);
            d.connect(r.getPinB(), null);

            circuit.preCompile();

            testElements(circuit.getElements(), answersZero, getIfFails(circuit));//check in 0ms

            //Simulate 250 ms.
            circuit.tick(0.25);
            testElements(circuit.getElements(), answersRev, getIfFails(circuit));//check in 250ms

            //Simulate 250 ms.
            circuit.tick(0.25);
            testElements(circuit.getElements(), answersZero, getIfFails(circuit));//check in 500ms

            //Simulate 250 ms.
            circuit.tick(0.25);
            testElements(circuit.getElements(), answersFwd, getIfFails(circuit));//check in 750ms

            //Simulate 250 ms.
            circuit.tick(0.25);
            testElements(circuit.getElements(), answersZero, getIfFails(circuit));//check in 1000ms

            Main.printAllElements(circuit);
        }

        @Test
        @Benchmark.benchmark
        @Order(5)
        void testFullWaveRectifier(){
            Circuit c = Circuits.Diodes.getFullHaveRectifier();
            c.setTickRate(0.005);
            c.preCompile();

            ElementOMeter meter = new ElementOMeter(c.getElements()[6]);
            ElementOMeter vmeter = new ElementOMeter(c.getElements()[0]);

            for (int i = 0; i < Math.floor(0.5 / c.getTickRate()); i++) {
                c.tick();
                meter.tick(c.getTickRate());
                vmeter.tick(c.getTickRate());
            }

            assertTrue(CircuitTest.equals(10.363796438309693, meter.getVoltage(ElementOMeter.method.average, null)));//voltage average
            assertTrue(CircuitTest.equals(10.473919519838212,meter.getVoltage(ElementOMeter.method.rms, c.getTickRate())));//voltage rms
            assertTrue(CircuitTest.equals(12.0 / Math.sqrt(2), vmeter.getVoltage(ElementOMeter.method.rms, c.getTickRate())));//rms of current source.

            Main.printAllElements(c);
        }

        @Test
        @Benchmark.benchmark
        @Order(6)
        void testVoltagePump(){
            Circuit c = Circuits.Diodes.getVoltageMultiplayer();
            c.preCompile();

            c.tick(2);
            CurrentSource voltageMeter = null;
            for (Element element : c.getElements()) {
                if (element instanceof CurrentSource source) {
                    voltageMeter = source;
                    break;
                }
            }
            if (voltageMeter == null)
                throw new IllegalStateException("The circuit haven't a CurrentSource");
            testElement(voltageMeter, new double[]{71.51,0,0});

            Main.printAllElements(c);
        }
    }

    @Nested
    @Benchmark.benchmark
    @Order(4)
    class Switches{
        @Test
        @Benchmark.benchmark
        @Order(1)
        void testResistenceSwitch(){
            double[][] openAnswers = {{800,8e-4,0.64},{800,8e-4,0.64},{0.08,8e-4,0}};
            double[][] closedAnswers = {{800,7.1428,5714.2857},{85.7136,7.1428,612.2351},{714.28,7.1428,5101.9591}};
            Circuit c = Circuits.Switches.getSwitchResistence();
            c.preCompile();

            testElements(c.getElements(), openAnswers, getIfFails(c));//test open
            for (Element element : c.getElements()) if (element instanceof Switch sw) sw.setOpen(false);
            c.tick();
            testElements(c.getElements(), closedAnswers, getIfFails(c));//test closed

            Main.printAllElements(c);
        }

        @Test
        @Benchmark.benchmark
        @Order(2)
        void testSwitchPWM() {
            double[][] answersInitial = {{50,0,0},{50,0,0},{0,0,0},{0,0,0}};
            double[][] answersFinal = {{50,0,0},{0,0,0},{50,0,0},{0,0,0}};
            new DynamicTest.DynamicData(Circuits.Switches::getPWM_switch, answersInitial, answersFinal, 2).setTickRate(0.005).test();
        }
    }

    @Nested
    @Benchmark.benchmark
    @Order(5)
    class Relay{
        @Test
        @Benchmark.benchmark
        @Order(1)
        void testPwmWithSquareWaveSource(){
            Circuit c = Circuits.Relays.getPwmWithSquareWaveSource();
            c.preCompile();

            ElementOMeter meter = new ElementOMeter(c.getElements()[4]);

            for (int i = 0; i < Math.floor(30 / c.getTickRate()); i++) {
                c.tick();
                meter.tick(c.getTickRate());
            }

            assertTrue(CircuitTest.equals(688.82, meter.getVoltage(ElementOMeter.method.average, null)));//voltage average
            assertTrue(CircuitTest.equals(4852.5061, meter.getPower(ElementOMeter.method.average, null)));//power average

            Main.printAllElements(c);
        }
    }

    @Nested
    @Benchmark.benchmark
    @Order(6)
    class BJT {
        @Test
        @Benchmark.benchmark
        @Order(1)
        void testBJT_NPN_forward(){
            double[][] answers = {{50,4.6},{5,-0.046},{0.4,4.6},{-4.6,-0.046},{23.02,4.6}};
            var c = Circuits.BJT.getNPNCircuit();
            ((VoltageSource) c.getElements()[1]).setSourceVoltage(5);
            c.preCompile();

            testElements(c.getElements(), answers, getIfFails(c));

            Main.printAllElements(c);
        }
        @Test
        @Benchmark.benchmark
        @Order(2)
        void testBJT_NPN_saturation(){
            double[][] answers = {{50,9.58,479.21},{10,-0.095,0.96},{0.416,9.58},{-9.58,-0.095,0.92},{47.92,9.58,459.29}};
            var c = Circuits.BJT.getNPNCircuit();
            c.preCompile();

            testElements(c.getElements(), answers, getIfFails(c));

            Main.printAllElements(c);
        }
        @Test
        @Benchmark.benchmark
        @Order(3)
        void testBJT_NPN_cutoff(){
            double[][] answers = {{50,0,0},{0,0,0},{0,0},{0,0},{0,0}};
            var c = Circuits.BJT.getNPNCircuit();
            ((VoltageSource) c.getElements()[1]).setSourceVoltage(0);
            c.preCompile();

            testElements(c.getElements(), answers, getIfFails(c));

            Main.printAllElements(c);
        }
        @Test
        @Benchmark.benchmark
        @Order(4)
        void testBJT_NPN_reverse(){
            double[][] answers = {{-50,0.4},{-10,-0.38},{-47.68,-0.4},{-37.68,-0.38},{-1.98,-0.4}};
            var c = Circuits.BJT.getNPNCircuit();
            ((VoltageSource) c.getElements()[0]).setSourceVoltage(-50);
            ((VoltageSource) c.getElements()[1]).setSourceVoltage(-10);
            c.preCompile();

            testElements(c.getElements(), answers, getIfFails(c));

            Main.printAllElements(c);
        }
    }
}