package eu.arrowhead.kalix.net.http;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TestHttpHeaders {
    @Test
    void shouldIgnoreNameCase() {
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
    }
}
