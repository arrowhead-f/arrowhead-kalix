package se.arkalix.codec.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.codec.binary.ByteArrayReader;
import se.arkalix.codec.binary.ByteArrayWriter;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestJsonArray {
    @ParameterizedTest
    @MethodSource("arraysToRead")
    void shouldReadArray(final JsonArray expected, final String json) {
        assertEquals(expected, JsonArray.decodeJson(new ByteArrayReader(json.getBytes(StandardCharsets.UTF_8))));
    }

    static Stream<Arguments> arraysToRead() {
        return Stream.of(
            arguments(new JsonArray(), "[]"),
            arguments(new JsonArray(new JsonNumber(1), new JsonString("x")), "[1,\"x\"]"),
            arguments(new JsonArray(new JsonArray(), new JsonArray(new JsonString("x"))), "[[],[\"x\"]]"),
            arguments(new JsonArray(JsonBoolean.TRUE, JsonBoolean.FALSE), " [ true , false ] "),
            arguments(new JsonArray(new JsonObject(new JsonPair("x", JsonNull.instance)), JsonNull.instance),
                "[{\"x\": null}, null]")
        );
    }

    @ParameterizedTest
    @MethodSource("arraysToWrite")
    void shouldWriteArray(final String expected, final JsonArray array) {
        final var writer = new ByteArrayWriter(new byte[expected.length()]);
        array.encodeJson(writer);
        assertEquals(expected, new String(writer.asByteArray(), StandardCharsets.UTF_8));
    }

    static Stream<Arguments> arraysToWrite() {
        return Stream.of(
            arguments("[]", new JsonArray()),
            arguments("[1,\"x\"]", new JsonArray(new JsonNumber(1), new JsonString("x"))),
            arguments("[[],[\"x\"]]", new JsonArray(new JsonArray(), new JsonArray(new JsonString("x")))),
            arguments("[true,false]", new JsonArray(JsonBoolean.TRUE, JsonBoolean.FALSE)),
            arguments("[{\"x\":null},null]", new JsonArray(new JsonObject(new JsonPair("x", JsonNull.instance)),
                JsonNull.instance))
        );
    }
}
