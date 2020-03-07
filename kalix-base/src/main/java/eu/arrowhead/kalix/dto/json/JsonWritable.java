package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.dto.binary.BinaryWriter;

import java.nio.ByteBuffer;

public interface JsonWritable extends DataWritable {
    void writeJson(BinaryWriter writer) throws WriteException;
}
