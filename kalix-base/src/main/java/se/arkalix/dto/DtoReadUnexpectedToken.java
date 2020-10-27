package se.arkalix.dto;

import java.util.Objects;

public class DtoReadUnexpectedToken extends DtoReadException {
    private final String message;
    private final String token;
    private final int offset;

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param target  Class the failed DTO read intended to instantiate.
     * @param reader  Encoding applied when reading failed.
     * @param message Description of failure.
     * @param token   String representation of offending token.
     * @param offset  Position of offending token in read source.
     */
    public DtoReadUnexpectedToken(
        final Class<? extends DtoReadable> target,
        final DtoReader reader,
        final String message,
        final String token,
        final int offset
    ) {
        this(target, reader, message, token, offset, null);
    }

    /**
     * Creates new {@link se.arkalix.dto DTO} read exception.
     *
     * @param target  Class the failed DTO read intended to instantiate.
     * @param reader  Encoding applied when reading failed.
     * @param message Description of failure.
     * @param token   String representation of offending token.
     * @param offset  Position of offending token in read source.
     * @param cause   Exception causing this exception to be thrown.
     */
    public DtoReadUnexpectedToken(
        final Class<? extends DtoReadable> target,
        final DtoReader reader,
        final String message,
        final String token,
        final int offset,
        final Throwable cause
    ) {
        super(reader, target, null, cause);
        this.message = Objects.requireNonNull(message, "message");
        this.token = Objects.requireNonNull(token, "token");
        this.offset = offset;
    }

    @Override
    public String getMessage() {
        return "Failed to read " + target().getName() + " from " +
            reader().encoding() + " source; the following issue occurred " +
            "when reading '" + token + "' at source offset " + offset + ": " +
            message;
    }

    /**
     * Gets the token read while this exception was thrown.
     *
     * @return Offending source token.
     */
    public String token() {
        return token;
    }

    /**
     * Gets the source offset at which the offending token is located.
     *
     * @return Offending token source offset.
     */
    public int offset() {
        return offset;
    }
}
