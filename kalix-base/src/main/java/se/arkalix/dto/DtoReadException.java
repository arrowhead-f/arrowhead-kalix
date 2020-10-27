package se.arkalix.dto;

import java.util.Objects;

/**
 * Signifies the failure to read one or more {@link se.arkalix.dto DTO class
 * instances} from some arbitrary source.
 */
public class DtoReadException extends DtoException {
    private final DtoReader reader;
    private final Class<? extends DtoReadable> target;

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param reader Encoding applied when reading failed.
     * @param target Class the failed DTO read intended to instantiate.
     */
    public DtoReadException(final DtoReader reader, final Class<? extends DtoReadable> target) {
        this(reader, target, null, null);
    }

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param reader  Encoding applied when reading failed.
     * @param target  Class the failed DTO read intended to instantiate.
     * @param message Description of failure.
     */
    public DtoReadException(final DtoReader reader, final Class<? extends DtoReadable> target, final String message) {
        this(reader, target, message, null);
    }

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param reader  DTO reader used when decoding failed.
     * @param target  Class the failed DTO read intended to instantiate.
     * @param message Description of failure.
     * @param cause   Exception causing this exception to be thrown.
     */
    public DtoReadException(
        final DtoReader reader,
        final Class<? extends DtoReadable> target,
        final String message,
        final Throwable cause
    ) {
        super(message, cause);
        this.target = Objects.requireNonNull(target, "target");
        this.reader = Objects.requireNonNull(reader, "reader");
    }

    @Override
    public String getMessage() {
        final var message = super.getMessage();
        return "Failed to read " + target + " from " + reader.encoding() +
            " source" + (message != null ? ": " + message : "");
    }

    /**
     * Gets the class the failed DTO read, represented by this exception, was
     * intended to create an instance of.
     *
     * @return Target DTO class.
     */
    public Class<? extends DtoReadable> target() {
        return target;
    }

    /**
     * Gets DTO reader that threw this exception.
     *
     * @return Throwing DTO reader.
     */
    public DtoReader reader() {
        return reader;
    }
}
