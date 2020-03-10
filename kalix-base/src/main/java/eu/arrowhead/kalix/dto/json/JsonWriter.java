package eu.arrowhead.kalix.dto.json;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.WriteException;
import eu.arrowhead.kalix.dto.binary.BinaryWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public final class JsonWriter {
    private JsonWriter() {}

    private static byte[] TRUE = new byte[]{'t', 'r', 'u', 'e'};
    private static byte[] FALSE = new byte[]{'f', 'a', 'l', 's', 'e'};
    private static byte[] HEX = new byte[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void write(final BigDecimal bigDecimal, final BinaryWriter target) {
        target.write(bigDecimal.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final BigInteger bigInteger, final BinaryWriter target) {
        target.write(bigInteger.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final boolean bool, final BinaryWriter target) {
        target.write(bool ? TRUE : FALSE);
    }

    public static void write(final long number, final BinaryWriter target) throws WriteException {
        if (number < -9007199254740991L || number > 9007199254740991L) {
            throw new WriteException(DataEncoding.JSON, "Only integers in " +
                "the range -(2^53 - 1) to (2^53 - 1) can be represented as " +
                "JSON numbers without loss of precision; " + number + " is " +
                "outside that range");
        }
        target.write(Long.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final double number, final BinaryWriter target) throws WriteException {
        if (!Double.isFinite(number)) {
            throw new WriteException(DataEncoding.JSON, "NaN, +Infinify and " +
                "-Infinity cannot be represented in JSON");
        }
        target.write(Double.toString(number)
            .getBytes(StandardCharsets.ISO_8859_1));
    }

    public static void write(final String string, final BinaryWriter target) {
        for (var b : string.getBytes(StandardCharsets.UTF_8)) {
            if (b < ' ') {
                target.write((byte) '\\');
                switch (b) {
                case '\b': b = 'b'; break;
                case '\f': b = 'f'; break;
                case '\n': b = 'n'; break;
                case '\r': b = 'r'; break;
                default:
                    target.write(new byte[]{'u', '0', '0'});
                    target.write(HEX[(b & 0xF0) >>> 4]);
                    target.write(HEX[(b & 0x0F)]);
                    continue;
                }
            }
            target.write(b);
        }
    }
}
