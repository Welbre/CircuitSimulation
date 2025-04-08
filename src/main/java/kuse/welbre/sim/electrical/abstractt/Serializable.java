package kuse.welbre.sim.electrical.abstractt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {
    void serialize(DataOutputStream st) throws IOException;
    void unSerialize(DataInputStream st) throws IOException;
}
