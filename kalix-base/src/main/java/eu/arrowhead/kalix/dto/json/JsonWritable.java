package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.DataWritable;
import eu.arrowhead.kalix.dto.WriteException;

import java.nio.ByteBuffer;

public interface JsonWritable extends DataWritable {
    void writeJson(ByteBuffer buffer) throws WriteException;
}
