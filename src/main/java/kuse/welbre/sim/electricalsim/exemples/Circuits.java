package kuse.welbre.sim.electricalsim.exemples;

import kuse.welbre.sim.electricalsim.*;
import kuse.welbre.sim.electricalsim.Element.Pin;

public final class Circuits {
    public static final class Resistors {
        public static Circuit getCircuit0(){
            Circuit circuit = new Circuit();
            VoltageSource v0 = new VoltageSource(32);
            VoltageSource v1 = new VoltageSource(20);
            Resistor r1 = new Resistor(2);
            Resistor r2 = new Resistor(4);
            Resistor r3 = new Resistor(8);

            circuit.addElement(v0,v1,r1,r2,r3);

            v0.connect(r2.getPinB(), r1.getPinA());
            v1.connect(r2.getPinA(), null);
            r1.connectB(null);
            r3.connect(null, r2.getPinB());

            return circuit;
        }

        public static Circuit getCircuit1(){
            Circuit circuit = new Circuit();
            VoltageSource v0 = new VoltageSource(32);
            CurrentSource i0 = new CurrentSource(3);
            Resistor r1 = new Resistor(2);
            Resistor r2 = new Resistor(4);
            Resistor r3 = new Resistor(8);

            Pin p1 = i0.getPinA();
            Pin p2 = v0.getPinB();

            v0.connect(p1,p2);
            i0.connect(p1,null);
            r1.connect(p1,null);
            r2.connect(p1,p2);
            r3.connect(p2, null);

            circuit.addElement(v0,i0,r1,r2,r3);

            return circuit;
        }
        public static Circuit getCircuit2(){
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

        public static Circuit getCircuit3(){
            Circuit circuit = new Circuit();
            VoltageSource v0 = new VoltageSource(30);
            CurrentSource i0 = new CurrentSource(2);
            Resistor r1 = new Resistor(5);
            Resistor r2 = new Resistor(3);
            Resistor r3 = new Resistor(10);

            circuit.addElement(v0,i0, r1,r2,r3);

            Pin p1 = v0.getPinA();
            Pin p2 = i0.getPinA();

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
        public static Circuit getCircuit4(){
            Circuit circuit = new Circuit();
            VoltageSource v0 = new VoltageSource(10);
            VoltageSource v1 = new VoltageSource(15);
            CurrentSource i0 = new CurrentSource(2);
            Resistor r1 = new Resistor(2);
            Resistor r2 = new Resistor(4);
            Resistor r3 = new Resistor(8);

            Pin p1 = v0.getPinA();
            Pin p2 = v1.getPinB();
            Pin p3 = v1.getPinA();

            v0.connect(p1,null);
            v1.connect(p3,p2);
            i0.connect(p2,p1);
            r1.connect(p1,p2);
            r2.connect(p2,null);
            r3.connect(p3,null);

            circuit.addElement(v0,v1,i0, r1,r2,r3);

            return circuit;
        }

        public static Circuit getCircuit5(){
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
    }
    public static final class Capacitors {
        public static Circuit getRcCircuit(){
            Circuit circuit = new Circuit();
            VoltageSource v0 = new VoltageSource(10);
            Resistor r = new Resistor(1);
            Capacitor c = new Capacitor(0.01);

            circuit.addElement(v0,r,c);
            v0.connect(r.getPinA(),null);
            c.connect(r.getPinB(),null);

            return circuit;
        }
        /**
         * <img src="doc-files/getAssociationCircuit.png" />
         */
        public static Circuit getAssociationCircuit(){
            Circuit circuit = new Circuit();
            VoltageSource v1 = new VoltageSource(36);
            Resistor r1 = new Resistor(12);
            Resistor r2 = new Resistor(12);
            Capacitor c1 = new Capacitor(0.300);
            Capacitor c2 = new Capacitor(0.300);
            Capacitor c3 = new Capacitor(0.020);
            Capacitor c4 = new Capacitor(400e-6);
            Capacitor c5 = new Capacitor(400e-6);
            Capacitor c6 = new Capacitor(400e-6);
            circuit.addElement(v1,r1,r2,c1,c2,c3,c4,c5,c6);

            Pin c2a = c2.getPinA();
            Pin r1a = r1.getPinA(), r1b = r1.getPinB();
            Pin r2b = r2.getPinB();

            v1.connect(r1b, null);

            c1.connect(c2.getPinB(), null);
            c3.connect(r1a, c2a);

            r2.connectA(c2a);
            c4.connect(r2b, null);
            c5.connect(r2b, null);
            c6.connect(r2b, null);

            return circuit;
        }

        public static Circuit getMultiplesCapacitorsCircuit(){
            Circuit circuit = new Circuit();
            var v1 = new VoltageSource(12);
            var v2 = new VoltageSource(-16);
            var r1 = new Resistor(12);
            var r2 = new Resistor(8);
            var r3 = new Resistor(30);
            var r4 = new Resistor(0.5);
            var c1 = new Capacitor(0.750);
            var c2 = new Capacitor(1);
            var c3 = new Capacitor(0.5);

            circuit.addElement(v1,v2,r1,r2,r3,r4,c1,c2,c3);

            v1.connect(c1.getPinA(), r3.getPinA());
            r3.connectB(null);
            var r3b = r3.getPinB();
            r1.connectB(r3b);
            r2.connectA(r3b);
            var r2b = r2.getPinB();
            var c1b = c1.getPinB();
            c3.connect(c1b, r1.getPinA());
            c2.connect(c1b, r2b);
            v2.connect(c1b, r4.getPinA());
            r4.connectB(r2b);

            return circuit;
        }
    }
    public static final class Inductors {
        public static Circuit getRlCircuit(){
            Circuit circuit = new Circuit();
            VoltageSource v0 = new VoltageSource(10);
            Resistor r = new Resistor(1);
            Inductor l = new Inductor(0.01);

            circuit.addElement(v0,r,l);
            v0.connect(r.getPinA(),null);
            l.connect(r.getPinB(),null);

            return circuit;
        }
    }
}
