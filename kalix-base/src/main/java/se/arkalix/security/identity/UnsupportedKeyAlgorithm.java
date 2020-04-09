package se.arkalix.security.identity;

public class UnsupportedKeyAlgorithm extends RuntimeException {
    public UnsupportedKeyAlgorithm(final String message) {
        super(message);
    }

    public UnsupportedKeyAlgorithm(final String message, final Throwable cause) {
        super(message, cause);
    }
}
