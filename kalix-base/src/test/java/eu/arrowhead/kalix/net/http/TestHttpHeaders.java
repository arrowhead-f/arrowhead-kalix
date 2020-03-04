package eu.arrowhead.kalix.net.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpHeaders {
    @Test
    void shouldEnforceLowercaseNames() {
        final var headers = new HttpHeaders()
            .set("Content-Type", "application/json")
            .set("Accept-Charset", "utf-8");

        final var expected0 = Optional.of("application/json");
        assertEquals(expected0, headers.get("Content-Type"));
        assertEquals(expected0, headers.get("CONTENT-TYPE"));
        assertEquals(expected0, headers.get("content-type"));

        final var expected1 = Optional.of("utf-8");
        assertEquals(expected1, headers.get("Accept-Charset"));
        assertEquals(expected1, headers.get("ACCEPT-CHARSET"));
        assertEquals(expected1, headers.get("accept-charset"));

        headers.forEach(entry ->
            assertTrue(entry.getKey().matches("[a-z-]+"), "`" + entry.getKey() + "` contains upper-case characters"));
    }

    @Test
    void shouldMergeAndSplitValuesCorrectly() {
        final var headers = new HttpHeaders()
            .add("accept", "application/json")
            .add("ACCEPT", "application/xml")
            .add("Accept", "*");

        final var values = headers.getAll("accept");
        assertTrue(values.isPresent());
        final var values0 = values.get();
        assertEquals(3, values0.length);
        assertEquals("application/json", values0[0]);
        assertEquals("application/xml", values0[1]);
        assertEquals("*", values0[2]);
    }

    @ParameterizedTest
    @MethodSource("mergeSplitValueProvider")
    void shouldSplitValuesCorrectly(final String value, final String[] expected) {
        final var headers = new HttpHeaders()
            .set("dummy", value);

        final var values = headers.getAll("dummy");
        assertTrue(values.isPresent());
        assertArrayEquals(expected, values.get());
    }

    static Stream<Arguments> mergeSplitValueProvider() {
        return Stream.of(
            arguments("plain", new String[]{"plain"}),
            arguments("a,b", new String[]{"a", "b"}),
            arguments("\"a\",\"b\"", new String[]{"a", "b"}),
            arguments("a, \"b, c\"", new String[]{"a", "b, c"}),
            arguments(" x,  y, \"  z \\\" w \"", new String[]{"x", "y", "  z \" w "})
        );
    }
}
