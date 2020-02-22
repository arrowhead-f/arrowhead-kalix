package eu.arrowhead.kalix.util.io.json;

public class JsonSyntaxError extends Exception {
    public JsonSyntaxError(final String message, final String value, final int offset) {
        super(message + " `" + value + "` at offset " + offset);
    }
}
