package se.arkalix.dto.json.value;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.dto.DtoReadException;
import se.arkalix.dto.DtoWriteException;
import se.arkalix.dto.binary.ByteArrayReader;
import se.arkalix.dto.binary.ByteArrayWriter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestJsonObject {
    @ParameterizedTest
    @MethodSource("objectsToRead")
    void shouldReadObject(final JsonObject expected, final String json) throws DtoReadException {
        assertEquals(expected, JsonObject.readJson(new ByteArrayReader(json.getBytes(StandardCharsets.UTF_8))));
    }

    static Stream<Arguments> objectsToRead() {
        return Stream.of(
            arguments(new JsonObject(), "{}"),
            arguments(new JsonObject(new JsonPair("x", new JsonNumber(1))), "{\"x\":1}"),
            arguments(new JsonObject(new JsonPair("x", JsonNull.instance()), new JsonPair("y",
                    new JsonObject(new JsonPair("z", new JsonNumber(Duration.ofSeconds(2)))))),
                "{\"x\":null, \"y\" :{ \"z\"  :   2   }}"),
            arguments(new JsonObject(new JsonPair("räksmörgås", new JsonNumber(Duration.ofMillis(54321)))),
                " { \"räksmörgås\" : 54.321 } "),
            arguments(new JsonObject(new JsonPair("x", new JsonString(Instant.ofEpochSecond(1586421651)))),
                "{\"x\":\"2020-04-09T08:40:51Z\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("objectsToWrite")
    void shouldWriteObject(final String expected, final JsonObject object) throws DtoWriteException {
        final var writer = new ByteArrayWriter(new byte[expected.getBytes(StandardCharsets.UTF_8).length]);
        object.writeJson(writer);
        assertEquals(expected, new String(writer.asByteArray(), StandardCharsets.UTF_8));
    }

    static Stream<Arguments> objectsToWrite() {
        return Stream.of(
            arguments("{}", new JsonObject()),
            arguments("{\"x\":1}", new JsonObject(new JsonPair("x", new JsonNumber(1)))),
            arguments("{\"x\":null,\"y\":{\"z\":2}}", new JsonObject(new JsonPair("x", JsonNull.instance()),
                new JsonPair("y", new JsonObject(new JsonPair("z", new JsonNumber(Duration.ofSeconds(2))))))),
            arguments("{\"räksmörgås\":54.321}", new JsonObject(new JsonPair("räksmörgås",
                new JsonNumber(Duration.ofMillis(54321))))),
            arguments("{\"x\":\"2020-04-09T08:40:51Z\"}", new JsonObject(new JsonPair("x",
                new JsonString(Instant.ofEpochSecond(1586421651)))))
        );
    }
}
