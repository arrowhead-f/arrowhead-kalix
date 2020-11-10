package se.arkalix.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.codec.binary.BinaryReader;

import java.util.Objects;

/**
 * Represents the inability to decode some string of bytes, caused by the
 * string of bytes not conforming to the codec it is expected to use.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces unless debug-level logging is enabled for this class</i>. If an HTTP
 * request causes an error that should always generate a stack trace, some
 * other exception type should be used instead.
 */
public class DecoderReadUnexpectedToken extends CodecException {
    private static final Logger logger = LoggerFactory.getLogger(DecoderReadUnexpectedToken.class);

    private final BinaryReader reader;
    private final String token;
    private final int offset;
    private final String description;

    /**
     * Creates new exception.
     *
     * @param codecType    Codec not adhered to.
     * @param reader      Reader allowing for the source in question to be read.
     * @param token       Representation of bytes causing decoding to fail.
     * @param offset      Reader position at which reading failed.
     * @param description Description of failure.
     */
    public DecoderReadUnexpectedToken(
        final CodecType codecType,
        final BinaryReader reader,
        final String token,
        final int offset,
        final String description
    ) {
        this(codecType, reader, token, offset, description, null);
    }

    /**
     * Creates new exception.
     *
     * @param codecType    Codec not adhered to.
     * @param reader      Reader allowing for the source in question to be read.
     * @param token       Representation of bytes causing decoding to fail.
     * @param offset      Reader position at which reading failed.
     * @param description Description of failure.
     */
    public DecoderReadUnexpectedToken(
        final CodecType codecType,
        final BinaryReader reader,
        final String token,
        final int offset,
        final String description,
        final Throwable cause
    ) {
        super(codecType, null, cause, true, logger.isDebugEnabled());
        this.reader = Objects.requireNonNull(reader, "reader");
        this.token = Objects.requireNonNull(token, "token");
        this.offset = offset;
        this.description = Objects.requireNonNull(description, "description");
    }

    /**
     * Gets reader containing the offending token.
     * <p>
     * The reader is <i>not</i> guaranteed to be positioned at the offending
     * token.
     *
     * @return Reader containing offending token.
     */
    public BinaryReader reader() {
        return reader;
    }

    /**
     * Gets representation of token (a string of related bytes), encountered
     * during decoding, causing this exception to be thrown.
     *
     * @return Offending token.
     */
    public String token() {
        return token;
    }

    /**
     * Gets offset, from the beginning of the decoded source, at which the
     * offending {@link #token() token} was encountered.
     *
     * @return Token offset.
     */
    public int offset() {
        return offset;
    }

    @Override
    public String getMessage() {
        return "Decoding " + codec() + " failed at source offset " +
            offset + " '" + token + "'; " + description;
    }
}
