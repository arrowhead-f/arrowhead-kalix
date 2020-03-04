package eu.arrowhead.kalix.net.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpVersion {
    @ParameterizedTest
    @MethodSource("cachedVersionProvider")
    void shouldReturnCachedVersion(final String version, final HttpVersion expected) {
        final var actual = HttpVersion.valueOf(version);
        assertSame(expected, actual);
    }

    static Stream<Arguments> cachedVersionProvider() {
        return Stream.of(
            arguments("HTTP/1.0", HttpVersion.HTTP_10),
            arguments("HTTP/1.1", HttpVersion.HTTP_11),
            arguments("HTTP/2.0", HttpVersion.HTTP_20)
        );
    }

    @ParameterizedTest
    @MethodSource("uncachedVersionProvider")
    void shouldReturnNewVersion(final String version) {
        final var version0 = HttpVersion.valueOf(version);
        final var version1 = HttpVersion.valueOf(version);
        assertNotSame(version0, version1);
        assertEquals(version0, version1);
    }

    static Stream<String> uncachedVersionProvider() {
        return Stream.of(
            "HTTP/1.2",
            "HTTP/2.1",
            "HTTP/3.2"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidVersionProvider")
    void shouldFailToParseVersion(final String version) {
        assertThrows(IllegalArgumentException.class, () -> HttpVersion.valueOf(version));
    }

    static Stream<String> invalidVersionProvider() {
        return Stream.of(
            "http/1.0",
            "HTTP 2.0",
            "1.1"
        );
    }
}
