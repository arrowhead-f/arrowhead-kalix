package se.arkalix.security.identity;

/**
 * Represents the failure to handle a public or private key associated with an
 * algorithm that is not supported.
 *
 * @see java.security.PublicKey
 * @see java.security.PrivateKey
 */
@SuppressWarnings("unused")
public class UnsupportedKeyAlgorithm extends RuntimeException {
    /**
     * @param message Human-readable description of why this exception was
     *                thrown.
     */
    public UnsupportedKeyAlgorithm(final String message) {
        super(message);
    }

    /**
     * @param message Human-readable description of why this exception was
     *                thrown.
     * @param cause   Any exception that caused this exception to be thrown.
     */
    public UnsupportedKeyAlgorithm(final String message, final Throwable cause) {
        super(message, cause);
    }
}
