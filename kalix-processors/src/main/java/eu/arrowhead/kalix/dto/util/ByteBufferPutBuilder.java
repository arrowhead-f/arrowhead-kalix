package eu.arrowhead.kalix.dto.util;

import com.squareup.javapoet.MethodSpec;

import java.nio.charset.StandardCharsets;

public class ByteBufferPutBuilder {
    private final StringBuilder builder = new StringBuilder();
    private final String byteBufferName;

    public ByteBufferPutBuilder(final String byteBufferName) {
        this.byteBufferName = byteBufferName;
    }

    public ByteBufferPutBuilder append(final char c) {
        builder.append(c);
        return this;
    }

    public ByteBufferPutBuilder append(final String string) {
        builder.append(string);
        return this;
    }

    public void clear() {
        builder.setLength(0);
    }

    public void addPutIfNotEmpty(final MethodSpec.Builder methodBuilder) {
        if (builder.length() == 0) {
            return;
        }
        final var input = builder.toString().getBytes(StandardCharsets.UTF_8);
        if (input.length == 1) {
            methodBuilder.addStatement(byteBufferName + ".put((byte) " + literalOf(input[0]) + ")");
        }
        else {
            final var output = new StringBuilder(builder.length() * 4);
            output.append(byteBufferName).append(".put(new byte[]{");
            for (var i = 0; i < input.length; ++i) {
                final var b = input[i];
                output.append(literalOf(b));
                if (i + 1 != input.length) {
                    output.append(", ");
                }
            }
            output.append("})");
            methodBuilder.addStatement(output.toString());
        }
        builder.setLength(0);
    }

    private String literalOf(final byte b) {
        if (b >= 0x20 && b <= 0x7E) {
            return "'" + ((char) b) + "'";
        }
        else {
            return "0x" + Integer.toHexString(b);
        }
    }
}
