package eu.arrowhead.kalix.dto;

public class WriteException extends Exception {
    public WriteException(final DataEncoding dataEncoding, final String message) {
        super("[" + dataEncoding + "] " + message);
    }
}
