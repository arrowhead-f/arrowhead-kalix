package se.arkalix.dto.json;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.ByteArrayWriter;
import se.arkalix.dto.json.value.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static se.arkalix.dto.DtoEncoding.JSON;

public class TestJsonWriter {
    @ParameterizedTest
    @MethodSource("objectsToWrite")
    void shouldWriteOne(final String expected, final JsonWritable input)
        throws DtoWriteException
    {
        final var writer = new ByteArrayWriter(new byte[expected.getBytes(StandardCharsets.UTF_8).length]);
        JSON.writer().writeOne(input, writer);

        assertEquals(expected, new String(writer.asByteArray(), StandardCharsets.UTF_8));
    }

    static Stream<Arguments> objectsToWrite() {
        return Stream.of(
            arguments("[]", new JsonArray()),
            arguments("{\"x\":1}", new JsonObject(new JsonPair("x", new JsonNumber(1))))
        );
    }

    @ParameterizedTest
    @MethodSource("manyObjectsToWrite")
    void shouldWriteMany(final String expected, final List<? extends JsonWritable> input)
        throws DtoWriteException
    {
        final var writer = new ByteArrayWriter(new byte[expected.getBytes(StandardCharsets.UTF_8).length]);
        JSON.writer().writeMany(input, writer);

        assertEquals(expected, new String(writer.asByteArray(), StandardCharsets.UTF_8));
    }

    static Stream<Arguments> manyObjectsToWrite() {
        return Stream.of(
            arguments("[[]]", List.of(new JsonArray())),
            arguments("[{\"x\":1}]", List.of(new JsonObject(new JsonPair("x", new JsonNumber(1))))),
            arguments("[[null],[true]]", List.of(new JsonArray(JsonNull.instance()),
                new JsonArray(JsonTrue.instance())))
        );
    }
}
