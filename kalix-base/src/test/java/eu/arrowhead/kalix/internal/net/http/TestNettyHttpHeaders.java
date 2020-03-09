package eu.arrowhead.kalix.internal.net.http;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestNettyHttpHeaders {
    @Test
    void shouldIgnoreNameCase() {
        final var headers = new NettyHttpHeaders(new DefaultHttpHeaders())
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
    }
}
