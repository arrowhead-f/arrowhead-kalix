package se.arkalix.net.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpMethod {
    @ParameterizedTest
    @MethodSource("cachedMethodProvider")
    void shouldReturnCachedMethod(final String method, final HttpMethod expected) {
        final var actual = HttpMethod.valueOf(method);
        assertSame(expected, actual);
    }

    static Stream<Arguments> cachedMethodProvider() {
        return Stream.of(
            arguments("GET", HttpMethod.GET),
            arguments("POST", HttpMethod.POST),
            arguments("PUT", HttpMethod.PUT),
            arguments("DELETE", HttpMethod.DELETE),
            arguments("HEAD", HttpMethod.HEAD),
            arguments("OPTIONS", HttpMethod.OPTIONS),
            arguments("CONNECT", HttpMethod.CONNECT),
            arguments("PATCH", HttpMethod.PATCH),
            arguments("TRACE", HttpMethod.TRACE)
        );
    }

    @ParameterizedTest
    @MethodSource("uncachedMethodProvider")
    void shouldReturnNewMethod(final String method) {
        final var method0 = HttpMethod.valueOf(method);
        final var method1 = HttpMethod.valueOf(method);
        assertNotSame(method0, method1);
        assertEquals(method0, method1);
    }

    static Stream<String> uncachedMethodProvider() {
        return Stream.of(
            "get",
            "Post",
            "puT",
            "dELETE",
            "SEND",
            "PING",
            "PUBLISH",
            "SUBSCRIBE"
        );
    }
}
