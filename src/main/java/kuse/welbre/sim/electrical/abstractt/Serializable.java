package kuse.welbre.sim.electrical.abstractt;

import kuse.welbre.sim.electrical.elements.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {
    enum SerialTypeEnum {
        VoltageSource(VoltageSource.class),
        ACVoltageSource(ACVoltageSource.class),
        SquareVoltageSource(SquareVoltageSource.class),
        CurrentSource(CurrentSource.class),
        CCCS(CCCS.class),
        CCVS(CCVS.class),
        VCCS(VCCS.class),
        VCVS(VCVS.class),
        Resistor(Resistor.class),
        Capacitor(Capacitor.class),
        Inductor(Inductor.class),
        Relay(Relay.class),
        Switch(Switch.class),
        Diode(Diode.class),
        BJTransistor(BJTransistor.class);

        public final Class<? extends Element> aClass;
        SerialTypeEnum(Class<? extends Element> clazz) {
            aClass = clazz;
        }

        public static short fromClass(Class<?> clazz){
            SerialTypeEnum[] values = SerialTypeEnum.values();
            for (int i = 0; i < values.length; i++) {
                if (values[i].aClass == clazz)
                    return (short) i;
            }
            throw new RuntimeException("%s isn't a registered class in SerialTypeEnum!".formatted(clazz.getName()));
        }
    }
    void serialize(DataOutputStream s) throws IOException;
    void unSerialize(DataInputStream s) throws IOException;
}
