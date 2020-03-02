package eu.arrowhead.kalix.internal.charset;

import java.io.ByteArrayOutputStream;

/**
 * Various Unicode-related utilities.
 */
public class Unicode {
    private Unicode() {}

    /**
     * Writes provided {@code char} as UTF-8 to given {@code stream}.
     *
     * @param c      Character to convert and write.
     * @param stream Target output stream.
     */
    public static void writeAsUtf8To(char c, final ByteArrayOutputStream stream) {
        writeAsUtf8To((int) c, stream);
    }

    /**
     * Writes provided Unicode code point as UTF-8 to given {@code stream}.
     *
     * @param cp     Code point to convert and write.
     * @param stream Target output stream.
     */
    public static void writeAsUtf8To(int cp, final ByteArrayOutputStream stream) {
        if (cp < 0) {
            throw new IllegalArgumentException("Negative code point provided");
        }
        else if (cp < 0x80) {
            stream.write(cp);
        }
        else {
            final var bytes = new byte[6];
            var b0 = 0;
            var prefix = 0xC0;
            var mask = 0x1F;
            while (true) {
                var b = 0x80 | (cp & 0x3F);
                bytes[b0++] = (byte) b;
                cp >>= 6;

                if ((cp & ~mask) == 0) {
                    bytes[b0++] = (byte) (prefix | cp);
                    break;
                }

                prefix = 0x80 | (prefix >> 1);
                mask >>= 1;
            }
            stream.write(bytes, 0, b0);
        }
    }
}
