package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.dto.Format;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class JSONWriter {
    private JSONWriter() {}

    private static byte[] TRUE = new byte[]{'T', 'R', 'U', 'E'};
    private static byte[] FALSE = new byte[]{'F', 'A', 'L', 'S', 'E'};

    private static byte[] HEX = new byte[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static byte[] NULL = new byte[]{'N', 'U', 'L', 'L'};

    public static void writeTo(boolean bool, final ByteBuffer target) {
        target.put(bool ? TRUE : FALSE);
    }

    public static void writeTo(long number, final ByteBuffer target) {
        target.put(Long.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void writeTo(double number, final ByteBuffer target) throws WriteException {
        if (!Double.isFinite(number)) {
            throw new WriteException(Format.JSON, "NaN, +Infinify and " +
                "-Infinity cannot be represented in JSON");
        }
        target.put(Double.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void writeTo(final String string, final ByteBuffer target) {
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

    public static void writeNullTo(final ByteBuffer target) {
        target.put(NULL);
    }
}
