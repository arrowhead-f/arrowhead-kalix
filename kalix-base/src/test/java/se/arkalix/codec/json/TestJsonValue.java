package se.arkalix.codec.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.codec.CodecType;
import se.arkalix.codec.binary.ByteArrayReader;
import se.arkalix.codec.binary.ByteArrayWriter;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestJsonValue {
    @ParameterizedTest
    @MethodSource("valuesToRead")
    void shouldReadOne(final JsonValue expected, final String input) {
        final var reader = new ByteArrayReader(input.getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals(expected, JsonValue.readJson(reader));
    }

    static Stream<Arguments> valuesToRead() {
        return Stream.of(
            arguments(new JsonArray(), "[]"),
            arguments(new JsonObject(new JsonPair("x", new JsonNumber(1))), "{\"x\":1}"),
            arguments(
                new JsonObject(new JsonPair("nl", new JsonString("\n"))),
                "{\"nl\":\"\\n\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("nl", new JsonString("1\n"))),
                "{\"nl\":\"1\\n\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("nl", new JsonString("1\n2"))),
                "{\"nl\":\"1\\n2\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("nl", new JsonString("\n2"))),
                "{\"nl\":\"\\n2\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("text", new JsonString("A sentence of arbitrary text\n"))),
                "{\"text\":\"A sentence of arbitrary text\\n\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("one", new JsonString("1"))),
                "{\"one\":\"\\u0031\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("oe", new JsonString("1ö"))),
                "{\"oe\":\"1\\u00F6\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("12", new JsonString("1ᛗ2"))),
                "{\"12\":\"1\\u16D72\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("arrow", new JsonString("↶2"))),
                "{\"arrow\":\"\\u21b62\"}"
            ),
            arguments(
                new JsonObject(new JsonPair("text", new JsonString("A sentence of arbitrary text⇶"))),
                "{\"text\":\"A sentence of arbitrary text\\u21F6\"}"
            )
        );
    }

    @ParameterizedTest
    @MethodSource("valuesToWrite")
    void shouldWriteOne(final String expected, final JsonValue input) {
        final var writer = new ByteArrayWriter(new byte[expected.getBytes(StandardCharsets.UTF_8).length]);
        final var codec = input.writeJson(writer);

        assertEquals(CodecType.JSON, codec);
        assertEquals(expected, new String(writer.asByteArray(), StandardCharsets.UTF_8));
    }

    static Stream<Arguments> valuesToWrite() {
        return Stream.of(
            arguments("[]", new JsonArray()),
            arguments("{\"x\":1}", new JsonObject(new JsonPair("x", new JsonNumber(1))))
        );
    }
}
