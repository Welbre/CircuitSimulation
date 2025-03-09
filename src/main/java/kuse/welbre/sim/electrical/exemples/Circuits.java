package kuse.welbre.sim.electrical.exemples;

import kuse.welbre.sim.electrical.*;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.abstractt.Element.Pin;
import kuse.welbre.sim.electrical.elements.*;

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

        public static Circuit getAssociationCircuit(){
            Circuit circuit = new Circuit();
            VoltageSource v1 = new VoltageSource(36);
            Resistor r1 = new Resistor(12);
            Resistor r2 = new Resistor(12);
            Inductor l1 = new Inductor(0.300);
            Inductor l2 = new Inductor(0.300);
            Inductor l3 = new Inductor(0.020);
            Inductor l4 = new Inductor(400e-6);
            Inductor l5 = new Inductor(400e-6);
            Inductor l6 = new Inductor(400e-6);
            circuit.addElement(v1,r1,r2,l1,l2,l3,l4,l5,l6);

            Pin c2a = l2.getPinA();
            Pin r1a = r1.getPinA(), r1b = r1.getPinB();
            Pin r2b = r2.getPinB();

            v1.connect(r1b, null);

            l1.connect(l2.getPinB(), null);
            l3.connect(r1a, c2a);

            r2.connectA(c2a);
            l4.connect(r2b, null);
            l5.connect(r2b, null);
            l6.connect(r2b, null);

            return circuit;
        }
    }
    public static final class RLC {
        /**
         * Version 4
         * SHEET 1 880 680
         * WIRE 16 128 -96 128
         * WIRE 192 128 96 128
         * WIRE 320 128 256 128
         * WIRE 160 208 -96 208
         * WIRE 400 208 400 128
         * WIRE 400 208 160 208
         * FLAG 160 208 0
         * SYMBOL voltage -96 112 R0
         * WINDOW 123 0 0 Left 0
         * WINDOW 39 0 0 Left 0
         * SYMATTR InstName V1
         * SYMATTR Value 12
         * SYMBOL res 112 112 R90
         * WINDOW 0 0 56 VBottom 2
         * WINDOW 3 32 56 VTop 2
         * SYMATTR InstName R1
         * SYMATTR Value 1
         * SYMBOL cap 256 112 R90
         * WINDOW 0 0 32 VBottom 2
         * WINDOW 3 32 32 VTop 2
         * SYMATTR InstName C1
         * SYMATTR Value 0.01
         * SYMBOL ind 416 112 R90
         * WINDOW 0 5 56 VBottom 2
         * WINDOW 3 32 56 VTop 2
         * SYMATTR InstName L1
         * SYMATTR Value 100m
         * TEXT -128 232 Left 2 !.tran 0 1 0 0.005 startup
         */
        public static Circuit getSeries(){
            Circuit circuit = new Circuit();
            VoltageSource v1 = new VoltageSource(12);
            Resistor r1 = new Resistor(1);
            Capacitor c1 = new Capacitor(0.010);
            Inductor l1 = new Inductor(0.1);

            circuit.addElement(v1,r1,c1,l1);

            v1.connect(r1.getPinB(), null);
            r1.connectA(c1.getPinB());
            c1.connectA(l1.getPinA());
            l1.connectB(null);

            return circuit;
        }

        public static Circuit getSeriesNoOscillation(){
            Circuit circuit = getSeries();
            ((Resistor) circuit.getElements()[1]).setResistance(6);

            return circuit;
        }

        public static Circuit getParallel(){
            Circuit circuit = new Circuit();
            VoltageSource v1 = new VoltageSource(12);
            Resistor r1 = new Resistor(1);
            Resistor r2 = new Resistor(1);
            Capacitor c1 = new Capacitor(0.010);
            Inductor l1 = new Inductor(0.1);

            circuit.addElement(v1,r1,r2,c1,l1);

            v1.connect(r1.getPinA(), null);
            Pin b = r1.getPinB();

            r2.connect(b, null);
            c1.connect(b, null);
            l1.connect(b, null);

            return circuit;
        }
    }
    public static final class CurrentControlledCurrentSources {
        public static Circuit getCCCSWithResistors(){
            Circuit circuit = new Circuit();
            var v1 = new VoltageSource(10);
            var r1 = new Resistor(10);
            var cccs1 = new CCCS(3);
            var r2 = new Resistor(100);

            circuit.addElement(v1,r1,cccs1,r2);

            v1.connect(r1.getPinA(), null);
            cccs1.connect(r2.getPinA(), null, r1.getPinB(),null);
            r2.connectB(null);

            return circuit;
        }

        public static Circuit getVCVSWithResistors(){
            Circuit circuit = new Circuit();
            var v1 = new VoltageSource(5);
            var r1 = new Resistor(5);
            var r2 = new Resistor(10);
            var vcvs = new VCVS(3);
            var r3 = new Resistor(50);

            circuit.addElement(v1,r1,r2,vcvs,r3);

            v1.connect(r1.getPinA(), null);
            r2.connect(r1.getPinB(), null);
            vcvs.connect(r3.getPinA(), null, r2.getPinA(), null);
            r3.connectB(null);

            return circuit;
        }

        public static Circuit getCCVSWithResistors(){
            Circuit circuit = new Circuit();
            var v1 = new VoltageSource(1000);
            var r1 = new Resistor(1000);
            var ccvs = new CCVS(2);
            var r2 = new Resistor(0.500);

            circuit.addElement(v1,r1,ccvs,r2);

            ccvs.connectA(r2.getPinA());
            ccvs.connectB(null);
            ccvs.connectC(r1.getPinB());
            ccvs.connectD(null);

            v1.connect(r1.getPinA(), null);
            r2.connectB(null);

            return circuit;
        }

        public static Circuit getVCCSWithResistors(){
            Circuit circuit = new Circuit();
            var v1 = new VoltageSource(1000);
            var ccvs = new VCCS(1e-3);
            var r1 = new Resistor(0.500);

            circuit.addElement(v1,ccvs,r1);

            v1.connectB(null);
            r1.connectB(null);
            ccvs.connect(r1.getPinA(), null, v1.getPinA(), null);

            return circuit;
        }
    }
    public static final class Diodes{
        public static Circuit getDiodeResistence(){
            Circuit circuit = new Circuit();
            VoltageSource v = new VoltageSource(10);
            Resistor r = new Resistor(1);
            Diode d = new Diode();

            v.connect(r.getPinA(), null);
            d.connect(r.getPinB(), null);

            circuit.addElement(v,r,d);

            return circuit;
        }
        public static Circuit getDiodeReverseBias(){
            Circuit circuit = getDiodeResistence();
            ((VoltageSource) circuit.getElements()[0]).setSourceVoltage(-10);

            return circuit;
        }
        public static Circuit getSeriesDiode(){
            Circuit circuit = new Circuit();
            VoltageSource v = new VoltageSource(10);
            Diode d0 = new Diode();
            Diode d1 = new Diode();
            Resistor r = new Resistor(10);

            v.connect(d0.getPinA(), null);
            d1.connect(d0.getPinB(), r.getPinA());
            r.connectB(null);

            circuit.addElement(v,d0,d1,r);

            return circuit;
        }
        public static Circuit getHalfWaveRectifier(){
            Circuit circuit = new Circuit();

            ACVoltageSource vc = new ACVoltageSource(12,5);//temporary change
            Diode d = new Diode();
            Capacitor c = new Capacitor(0.05);
            Resistor r = new Resistor(2);

            vc.connect(d.getPinA(), null);
            Pin b = d.getPinB();
            c.connect(b,null);
            r.connect(b, null);

            circuit.addElement(vc,d,c,r);
            return circuit;
        }
        public static Circuit getFullHaveRectifier(){
            Circuit c = new Circuit();
            ACVoltageSource v = new ACVoltageSource(12,2);
            Diode d00 = new Diode();
            Diode d01 = new Diode();
            Diode d10 = new Diode();
            Diode d11 = new Diode();
            Capacitor ka = new Capacitor(0.5);
            Resistor r = new Resistor(2);

            c.addElement(v,d00,d01,d10,d11,ka,r);

            ka.connectB(null);

            Element.Pin inA = v.getPinA();
            Element.Pin inB = v.getPinB();
            Element.Pin outA = ka.getPinA();
            Element.Pin outB = ka.getPinB();

            d00.connect(inA,outA);
            d01.connect(outB,inA);
            d10.connect(inB,outA);
            d11.connect(outB, inB);
            r.connect(outA,outB);

            return c;
        }
    }
}
