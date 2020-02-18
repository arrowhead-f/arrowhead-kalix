package eu.arrowhead.kalix.http;

import eu.arrowhead.kalix.description.InterfaceDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpPattern {
    @Test
    void shouldMatchRootPattern() {
        assertTrue(HttpPattern.valueOf("/").apply("/").isPresent());
        assertTrue(HttpPattern.valueOf("/").apply("").isPresent());
        assertTrue(HttpPattern.valueOf("").apply("/").isPresent());
        assertTrue(HttpPattern.valueOf("").apply("").isPresent());
    }

    @Test
    void shouldNotMatchRootPattern() {
        assertFalse(HttpPattern.valueOf("/").apply("/some-path").isEmpty());
        assertFalse(HttpPattern.valueOf("/").apply("another-path").isEmpty());
        assertFalse(HttpPattern.valueOf("").apply("/maybe").isEmpty());
        assertFalse(HttpPattern.valueOf("").apply("maybe-not").isEmpty());
    }

    @ParameterizedTest
    @MethodSource("validPatternPathParameterSets")
    void shouldMatchPattern(final String pattern, final String path, final String[] parameters) {
        final var actualParameters = HttpPattern.valueOf(pattern)
            .apply(path)
            .orElseGet(Collections::emptyList);

        assertEquals(Arrays.asList(parameters), actualParameters);
    }

    static Stream<Arguments> validPatternPathParameterSets() {
        return Stream.of(
            //arguments("/some/path", "/some/path", new String[0]),
            //arguments("/some/path", "some/path", new String[0]),
            //arguments("some/path", "/some/path", new String[0]),
            //arguments("some/path", "some/path", new String[0]),

            //arguments("/#0", "/hello", new String[]{"hello"})
            arguments("/#@/#%", "/swirl/percent", new String[]{"swirl", "percent"})
            //arguments("/#A/#B/#C", "x/y/z", new String[]{"x", "y", "z"})

        );
    }

    static Stream<String> badTripletProvider() {
        return Stream.of(
            "HTTP/SECURE/JSON",
            "HTTPS-JSON",
            "HTTPS-TLS-JSON",
            "HTTP_SECURE_JSON",
            "MQTT-TLS-CBOR"
        );
    }

}
