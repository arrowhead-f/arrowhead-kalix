package se.arkalix.dto;

import se.arkalix.dto.binary.BinaryWriter;
import se.arkalix.net.Encoding;

import java.util.List;

/**
 * An object useful for writing {@link se.arkalix.dto DTO class instances} to
 * targets representing their contents with a specific encoding.
 */
public interface DtoWriter {
    /**
     * Encoding written by this DTO writer.
     *
     * @return Written encoding.
     */
    Encoding encoding();

    /**
     * Attempts to encode {@code value} and write it to {@code target}.
     *
     * @param value  Object to encode and write to {@code target}.
     * @param target Receiver of encoded form of {@code value}.
     * @throws DtoWriteException             If writing to {@code target} fails.
     * @throws NullPointerException          If {@code value} or {@code target}
     *                                       is {@code null}.
     * @throws UnsupportedOperationException If the DTO interface type of
     *                                       {@code value} does not support the
     *                                       encoding of this writer.
     */
    <T extends DtoWritable> void writeOne(final T value, final BinaryWriter target) throws DtoWriteException;

    /**
     * Attempts to encode {@code values} and write them to {@code target}.
     *
     * @param values Objects to encode and write to {@code target}.
     * @param target Receiver of encoded form of {@code values}.
     * @throws DtoWriteException             If writing to {@code target} fails.
     * @throws NullPointerException          If {@code values} or
     *                                       {@code target} is {@code null}.
     * @throws UnsupportedOperationException If the DTO interface type of
     *                                       {@code values} does not support
     *                                       the encoding of this writer.
     */
    <T extends DtoWritable> void writeMany(final List<T> values, final BinaryWriter target) throws DtoWriteException;
}
