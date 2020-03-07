package eu.arrowhead.kalix.dto.util;

import com.squareup.javapoet.MethodSpec;

import java.nio.charset.StandardCharsets;

public class BinaryWriterWriteCache {
    private final StringBuilder builder = new StringBuilder();
    private final String writerName;

    public BinaryWriterWriteCache(final String writerName) {
        this.writerName = writerName;
    }

    public BinaryWriterWriteCache append(final char c) {
        builder.append(c);
        return this;
    }

    public BinaryWriterWriteCache append(final String string) {
        builder.append(string);
        return this;
    }

    public void clear() {
        builder.setLength(0);
    }

    public void addPut(final MethodSpec.Builder methodBuilder) {
        final var input = builder.toString().getBytes(StandardCharsets.UTF_8);
        if (input.length == 1) {
            methodBuilder.addStatement(writerName + ".write((byte) " + literalOf(input[0]) + ")");
        }
        else {
            final var output = new StringBuilder(builder.length() * 4);
            output.append(writerName).append(".write(new byte[]{");
            for (var i = 0; i < input.length; ++i) {
                if (i != 0) {
                    output.append(", ");
                }
                output.append(literalOf(input[i]));
            }
            output.append("})");
            methodBuilder.addStatement(output.toString());
        }
        builder.setLength(0);
    }

    public void addWriteIfNotEmpty(final MethodSpec.Builder methodBuilder) {
        if (builder.length() > 0) {
            addPut(methodBuilder);
        }
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
