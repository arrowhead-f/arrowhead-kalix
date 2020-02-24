package eu.arrowhead.kalix.util.io.json;

public class JsonSyntaxException extends Exception {
    public JsonSyntaxException(final String message, final String value, final int offset) {
        super(message + " `" + value + "` at offset " + offset);
    }
}
