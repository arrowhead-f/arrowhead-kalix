package se.arkalix.dto.json;

import se.arkalix.dto.DtoReadable;

/**
 * Represents a type that can be constructed from JSON input.
 * <p>
 * <i>Do not implement this interface directly.</i> It is implemented
 * automatically by generated {@link se.arkalix.dto DTO classes} where JSON is
 * specified as a readable encoding. Even though the interface specifies no
 * methods, it being implemented by a class signals that the class satisfies
 * certain requirements always fulfilled by the mentioned type of generated
 * DTO classes.
 *
 * @see <a href="https://tools.ietf.org/html/rfc8259">RFC 8259</a>
 */
public interface JsonReadable extends DtoReadable {}
