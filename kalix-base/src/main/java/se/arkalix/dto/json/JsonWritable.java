package se.arkalix.dto.json;

import se.arkalix.dto.DtoWritable;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.encoding.binary.BinaryWriter;

/**
 * Represents a type that can be written as JSON.
 * <p>
 * <i>Do not implement this interface directly.</i> It is implemented
 * automatically by generated {@link se.arkalix.dto DTO classes} where JSON is
 * specified as a writable encoding.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
public interface JsonWritable extends DtoWritable {
    void writeJson(BinaryWriter writer) throws DtoWriteException;
}
