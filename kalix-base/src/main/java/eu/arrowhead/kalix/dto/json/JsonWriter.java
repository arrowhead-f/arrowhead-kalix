package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.dto.Format;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public final class JsonWriter {
    private JsonWriter() {}

    private static byte[] TRUE = new byte[]{'t', 'r', 'u', 'e'};
    private static byte[] FALSE = new byte[]{'f', 'a', 'l', 's', 'e'};
    private static byte[] HEX = new byte[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void write(final boolean bool, final ByteBuffer target) {
        target.put(bool ? TRUE : FALSE);
    }

    public static void write(final long number, final ByteBuffer target) throws WriteException {
        if (number < -9007199254740991L ||number > 9007199254740991L) {
            throw new WriteException(Format.JSON, "Only integers in the " +
                "range -(2^53 - 1) to (2^53 - 1) can be represented as JSON " +
                "numbers without loss of precision; " + number + " is " +
                "outside that range");
        }
        target.put(Long.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final double number, final ByteBuffer target) throws WriteException {
        if (!Double.isFinite(number)) {
            throw new WriteException(Format.JSON, "NaN, +Infinify and " +
                "-Infinity cannot be represented in JSON");
        }
        target.put(Double.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final String string, final ByteBuffer target) {
        for (var b : string.getBytes(StandardCharsets.UTF_8)) {
            if (b < ' ') {
                target.put((byte) '\\');
                switch (b) {
                case '\b': b = 'b'; break;
                case '\f': b = 'f'; break;
                case '\n': b = 'n'; break;
                case '\r': b = 'r'; break;
                default:
                    target.put((byte) 'u');
                    target.put((byte) '0');
                    target.put((byte) '0');
                    target.put(HEX[(b & 0xF0) >>> 4]);
                    target.put(HEX[(b & 0x0F)]);
                    continue;
                }
            }
            target.put(b);
        }
    }
}
