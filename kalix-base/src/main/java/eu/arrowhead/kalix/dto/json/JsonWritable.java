package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.FormatWritable;
import eu.arrowhead.kalix.dto.WriteException;

import java.nio.ByteBuffer;

public interface JsonWritable extends FormatWritable {
    void writeJson(ByteBuffer buffer) throws WriteException;
}
