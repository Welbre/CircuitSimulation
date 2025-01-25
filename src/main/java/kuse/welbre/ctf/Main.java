package kuse.welbre.ctf;

import kuse.welbre.ctf.electricalsim.*;

public class Main {
    public static void main(String[] args) {
        Circuit circuit = capacitorTest();
        circuit.tick(1);
        printCircuitMatrix(circuit);
        printAllComponents(circuit);
    }

    private static Circuit capacitorTest(){
        Circuit circuit = new Circuit();
        VoltageSource v0 = new VoltageSource(10);
        Resistor r = new Resistor(100);
        Capacitor c = new Capacitor(0.001);

        circuit.addElement(v0,r,c);
        v0.connect(r.getPinA(),null);
        c.connect(r.getPinB(),null);

        return circuit;
    }

    private static Circuit loadCase1(){
        Circuit circuit = new Circuit();
        VoltageSource v0 = new VoltageSource(32);
        VoltageSource v1 = new VoltageSource(20);
        Resistor r1 = new Resistor(2);
        Resistor r2 = new Resistor(4);
        Resistor r3 = new Resistor(8);

        circuit.addElement(v0,v1,r1,r2,r3);

        v0.connect(r2.getPinA(), r1.getPinA());
        v1.connect(r2.getPinB(), null);
        r1.connectB(null);
        r3.connect(r2.getPinA(), null);

        return circuit;
    }

    private static Circuit loadCase2(){
        Circuit circuit = new Circuit();
        VoltageSource v0 = new VoltageSource(32);
        CurrentSource i0 = new CurrentSource(3);
        Resistor r1 = new Resistor(2);
        Resistor r2 = new Resistor(4);
        Resistor r3 = new Resistor(8);

        Element.Pin p1 = i0.getPinA();
        Element.Pin p2 = v0.getPinB();

        v0.connect(p1,p2);
        i0.connect(p1,null);
        r1.connect(p1,null);
        r2.connect(p1,p2);
        r3.connect(p2, null);

        circuit.addElement(v0,i0,r1,r2,r3);

        return circuit;
    }

    private static Circuit loadCase3(){
        Circuit circuit = new Circuit();
        VoltageSource v0 = new VoltageSource(40);
        CurrentSource i0 = new CurrentSource(1);
        Resistor r1 = new Resistor(2);
        Resistor r2 = new Resistor(9);
        Resistor r3 = new Resistor(8);
        Resistor r4 = new Resistor(10);
        Resistor r6 = new Resistor(4);

        circuit.addElement(v0,i0,r1,r2,r3,r4,r6);

        v0.connectB(null);
        i0.connectB(null);
        r1.connectA(v0.getPinA());
        r2.connectA(r1.getPinB());
        r3.connect(r2.getPinB(),i0.getPinA());
        r4.connect(r1.getPinB(),null);
        r6.connect(r2.getPinB(),null);
        return circuit;
    }

    private static Circuit loadCase4(){
        Circuit circuit = new Circuit();
        VoltageSource v0 = new VoltageSource(30);
        CurrentSource i0 = new CurrentSource(2);
        Resistor r1 = new Resistor(5);
        Resistor r2 = new Resistor(3);
        Resistor r3 = new Resistor(10);

        circuit.addElement(v0,i0, r1,r2,r3);

        Element.Pin p1 = v0.getPinA();
        Element.Pin p2 = i0.getPinA();

        v0.connect(p1,null);
        i0.connect(p2,null);
        r1.connect(p1,p2);
        r2.connect(p2,null);
        r3.connect(p2,null);
        return circuit;
    }

    /**
     * <img src="doc-files/c6.png" />
     */
    private static Circuit loadCase5(){
        Circuit circuit = new Circuit();
        VoltageSource v0 = new VoltageSource(10);
        VoltageSource v1 = new VoltageSource(15);
        CurrentSource i0 = new CurrentSource(2);
        Resistor r1 = new Resistor(2);
        Resistor r2 = new Resistor(4);
        Resistor r3 = new Resistor(8);

        Element.Pin p1 = v0.getPinA();
        Element.Pin p2 = v1.getPinB();
        Element.Pin p3 = v1.getPinA();

        v0.connect(p1,null);
        v1.connect(p3,p2);
        i0.connect(p2,p1);
        r1.connect(p1,p2);
        r2.connect(p2,null);
        r3.connect(p3,null);

        circuit.addElement(v0,v1,i0, r1,r2,r3);

        return circuit;
    }

    private static Circuit loadCase6(){
        Circuit circuit = new Circuit();
        CurrentSource i0 = new CurrentSource(0.01);
        CurrentSource i1 = new CurrentSource(0.01);
        Resistor r1 = new Resistor(500);

        i0.connectB(null);
        i1.connect(i0.getPinA(),null);
        r1.connect(i0.getPinA(),null);
        circuit.addElement(i0, i1, r1);
        return circuit;
    }


    private static void printCircuitMatrix(Circuit matrix){
        System.out.println("G matrix:");
        printMatrix(matrix.DEBUG_G);
        System.out.println("X matrix:");
        printMatrix(matrix.DEBUG_X);
        System.out.println("Z matrix:");
        printMatrix(matrix.DEBUG_Z);
    }

    private static void printMatrix(double[][] matrix){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                builder.append(String.format("%.3f", matrix[i][j])).append("\t\t\t");
            }
            builder.append("\n");
        }
        System.out.println(builder);
    }

    private static void printMatrix(double[] matrix){
        StringBuilder builder = new StringBuilder();
        for (double v : matrix) {
            builder.append(String.format("%.3f", v)).append("\n");
        }
        System.out.println(builder);
    }

    private static void printAllComponents(Circuit circuit){
        System.out.println("List of all elements: ");
        for (Element element : circuit.getElements()) {
            System.out.println(element);
        }
    }
}