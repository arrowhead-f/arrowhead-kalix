package se.arkalix.dto.json;

import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.BinaryWriter;

public interface JsonWritable extends DtoWritable {
    void writeJson(BinaryWriter writer) throws DtoWriteException;
}
