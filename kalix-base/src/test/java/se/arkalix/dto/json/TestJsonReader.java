package se.arkalix.dto.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.binary.ByteArrayReader;
import se.arkalix.dto.json.value.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static se.arkalix.dto.DtoEncoding.JSON;

public class TestJsonReader {
    @ParameterizedTest
    @MethodSource("objectsToRead")
    void shouldReadOne(final JsonReadable expected, final String input)
        throws DtoReadException
    {
        final var reader = new ByteArrayReader(input.getBytes(StandardCharsets.UTF_8));
        Assertions.assertEquals(expected, JSON.reader().readOne(expected.getClass(), reader));
    }

    static Stream<Arguments> objectsToRead() {
        return Stream.of(
            arguments(new JsonArray(), "[]"),
            arguments(new JsonObject(new JsonPair("x", new JsonNumber(1))), "{\"x\":1}")
        );
    }

    @ParameterizedTest
    @MethodSource("manyObjectsToRead")
    void shouldReadMany(final List<? extends JsonReadable> expected, final String input)
        throws DtoReadException
    {
        final var reader = new ByteArrayReader(input.getBytes(StandardCharsets.UTF_8));
        final var class_ = expected.get(0).getClass();
        assertEquals(expected, JSON.reader().readMany(class_, reader));
    }

    static Stream<Arguments> manyObjectsToRead() {
        return Stream.of(
            arguments(List.of(new JsonArray()), "[[]]"),
            arguments(List.of(new JsonObject(new JsonPair("x", new JsonNumber(1)))), "[{\"x\":1}]"),
            arguments(List.of(new JsonArray(JsonNull.instance), new JsonArray(JsonBoolean.TRUE)),
                "[[null],[true]]")
        );
    }
}
