package se.arkalix.dto;

import se.arkalix.encoding.binary.BinaryReader;
import se.arkalix.encoding.Encoding;

import java.util.List;

/**
 * An object useful for reading {@link se.arkalix.dto DTO class instances} from
 * sources representing their contents with a specific encoding.
 */
public interface DtoReader {
    /**
     * Encoding read by this DTO reader.
     *
     * @return Read encoding.
     */
    Encoding encoding();

    /**
     * Reads one encoded value from {@code source}.
     *
     * @param class_ Class of value to read.
     * @param source Source from which the desired value is to be read.
     * @return Decoded value.
     * @throws DtoReadException              If reading from {@code source}
     *                                       fails.
     * @throws NullPointerException          If {@code class_} or
     *                                       {@code source} is {@code null}.
     * @throws UnsupportedOperationException If the DTO interface type of
     *                                       {@code class_} does not support
     *                                       the encoding of this reader.
     */
    <T extends DtoReadable> T readOne(final Class<T> class_, final BinaryReader source) throws DtoReadException;

    /**
     * Reads a list of encoded values from {@code source}.
     *
     * @param class_ Class of values to read.
     * @param source Source from which the desired values are to be read.
     * @return Decoded values.
     * @throws DtoReadException              If reading from {@code source}
     *                                       fails.
     * @throws NullPointerException          If {@code class_} or
     *                                       {@code source} is {@code null}.
     * @throws UnsupportedOperationException If the DTO interface type of
     *                                       {@code class_} does not support
     *                                       the encoding of this reader.
     */
    <T extends DtoReadable> List<T> readMany(final Class<T> class_, final BinaryReader source) throws DtoReadException;
}
