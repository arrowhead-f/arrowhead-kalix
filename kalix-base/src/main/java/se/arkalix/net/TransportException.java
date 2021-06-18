package se.arkalix.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

/**
 * Signifies that a message transport could not be established or failed.
 * <p>
 * As these exceptions are expected to be quite common, and are caused by
 * external rather than internal mistakes, <i>they do not produce stack
 * traces unless debug-level logging is enabled for this class</i>. If an HTTP
 * request causes an error that should always generate a stack trace, some
 * other exception type should be used instead.
 */
@SuppressWarnings("unused")
public class TransportException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(TransportException.class);

    /**
     * Creates new exception form given {@link SSLHandshakeException}.
     * <p>
     * The given causing exception has its stack pruned from all entries except
     * for the topmost, unless debug-level logging is enabled for this class.
     *
     * @param cause SSL handshake exception causing this exception to be thrown.
     * @return New exception.
     */
    public static TransportException from(final SSLHandshakeException cause) {
        pruneStackTracesUnlessDebugLoggingIsEnabled(cause);
        return new TransportException("SSL/TLS handshake failed", cause);
    }

    /**
     * Creates new exception form given {@link SSLException}.
     * <p>
     * The given causing exception has its stack pruned from all entries except
     * for the topmost, unless debug-level logging is enabled for this class.
     *
     * @param cause SSL exception causing this exception to be thrown.
     * @return New exception.
     */
    public static TransportException from(final SSLException cause) {
        pruneStackTracesUnlessDebugLoggingIsEnabled(cause);
        return new TransportException("SSL/TLS failure", cause);
    }

    private static void pruneStackTracesUnlessDebugLoggingIsEnabled(Throwable throwable) {
        if (!logger.isDebugEnabled()) {
            while (throwable != null) {
                final var stackTrace = throwable.getStackTrace();
                if (stackTrace.length > 0) {
                    throwable.setStackTrace(new StackTraceElement[]{stackTrace[0]});
                }
                throwable = throwable.getCause();
            }
        }
    }

    /**
     * Creates new exception.
     *
     * @param description Description to associate with exception.
     */
    public TransportException(final String description) {
        this(description, null);
    }

    /**
     * Creates new exception.
     *
     * @param description Description to associate with exception.
     * @param cause       Exception causing this exception to be thrown.
     */
    public TransportException(final String description, final Throwable cause) {
        super(description, cause, true, logger.isDebugEnabled());
    }

    /**
     * Creates new exception.
     *
     * @param cause Exception causing this exception to be thrown.
     */
    public TransportException(final Throwable cause) {
        this(null, cause);
    }
}
