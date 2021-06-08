package se.arkalix.net.http._internal;

import org.junit.jupiter.api.Test;
import se.arkalix.net.http._internal.NettyHttpHeaders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestNettyHttpHeaders {
    @Test
    void shouldIgnoreNameCase() {
        final var headers = new NettyHttpHeaders()
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
