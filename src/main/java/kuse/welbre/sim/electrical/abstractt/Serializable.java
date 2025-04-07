package kuse.welbre.sim.electrical.abstractt;

import java.nio.ByteBuffer;

public interface Serializable {
    void serialize(ByteBuffer buffer);
    void unSerialize(ByteBuffer buffer);
}
