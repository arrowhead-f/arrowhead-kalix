package se.arkalix.util;

import se.arkalix.util.annotation.Internal;

/**
 * Exception thrown to indicate the existence of a bug in Arrowhead Kalix.
 */
public class InternalException extends RuntimeException {
    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public InternalException(final String message) {
        super(message);
    }

    /**
     * <i>Internal API</i>. Might change in breaking ways between patch
     * versions of the Kalix library. Use is not advised.
     */
    @Internal
    public InternalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}