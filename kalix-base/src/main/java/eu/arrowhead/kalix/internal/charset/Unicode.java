package eu.arrowhead.kalix.internal.charset;

import java.io.ByteArrayOutputStream;

public class Unicode {
    private Unicode() {}

    public static void writeAsUtf8To(char c, final ByteArrayOutputStream stream) {
        writeAsUtf8To((int) c, stream);
    }

    public static void writeAsUtf8To(int cp, final ByteArrayOutputStream stream) {
        if (cp < 0) {
            throw new IllegalStateException("Negative code point provided");
        }
        else if (cp < 0x80) {
            stream.write(cp);
        }
        else {
            final var bytes = new byte[8];
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
