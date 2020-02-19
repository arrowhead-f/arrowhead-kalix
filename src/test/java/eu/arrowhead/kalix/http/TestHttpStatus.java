package eu.arrowhead.kalix.http;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpStatus {
    @ParameterizedTest
    @MethodSource("cachedStatusProvider")
    void shouldReturnCachedStatus(final int status, final HttpStatus expected) {
        final var actual = HttpStatus.valueOf(status);
        assertSame(expected, actual);
    }

    static Stream<Arguments> cachedStatusProvider() {
        return Stream.of(
            arguments(100, HttpStatus.CONTINUE),
            arguments(101, HttpStatus.SWITCHING_PROTOCOLS),
            arguments(102, HttpStatus.PROCESSING),
            arguments(200, HttpStatus.OK),
            arguments(201, HttpStatus.CREATED),
            arguments(202, HttpStatus.ACCEPTED),
            arguments(203, HttpStatus.NON_AUTHORITATIVE_INFORMATION),
            arguments(204, HttpStatus.NO_CONTENT),
            arguments(205, HttpStatus.RESET_CONTENT),
            arguments(206, HttpStatus.PARTIAL_CONTENT),
            arguments(207, HttpStatus.MULTI_STATUS),
            arguments(208, HttpStatus.ALREADY_REPORTED),
            arguments(226, HttpStatus.IM_USED),
            arguments(300, HttpStatus.MULTIPLE_CHOICES),
            arguments(301, HttpStatus.MOVED_PERMANENTLY),
            arguments(302, HttpStatus.FOUND),
            arguments(303, HttpStatus.SEE_OTHER),
            arguments(304, HttpStatus.NOT_MODIFIED),
            arguments(305, HttpStatus.USE_PROXY),
            arguments(307, HttpStatus.TEMPORARY_REDIRECT),
            arguments(308, HttpStatus.PERMANENT_REDIRECT),
            arguments(400, HttpStatus.BAD_REQUEST),
            arguments(401, HttpStatus.UNAUTHORIZED),
            arguments(402, HttpStatus.PAYMENT_REQUIRED),
            arguments(403, HttpStatus.FORBIDDEN),
            arguments(404, HttpStatus.NOT_FOUND),
            arguments(405, HttpStatus.METHOD_NOT_ALLOWED),
            arguments(406, HttpStatus.NOT_ACCEPTABLE),
            arguments(407, HttpStatus.PROXY_AUTHENTICATION_REQUIRED),
            arguments(408, HttpStatus.REQUEST_TIMEOUT),
            arguments(409, HttpStatus.CONFLICT),
            arguments(410, HttpStatus.GONE),
            arguments(411, HttpStatus.LENGTH_REQUIRED),
            arguments(412, HttpStatus.PRECONDITION_FAILED),
            arguments(413, HttpStatus.PAYLOAD_TOO_LARGE),
            arguments(414, HttpStatus.URI_TOO_LONG),
            arguments(415, HttpStatus.UNSUPPORTED_MEDIA_TYPE),
            arguments(416, HttpStatus.RANGE_NOT_SATISFIABLE),
            arguments(417, HttpStatus.EXPECTATION_FAILED),
            arguments(418, HttpStatus.IM_A_TEAPOT),
            arguments(421, HttpStatus.MISDIRECTED_REQUEST),
            arguments(422, HttpStatus.UNPROCESSABLE_ENTITY),
            arguments(423, HttpStatus.LOCKED),
            arguments(424, HttpStatus.FAILED_DEPENDENCY),
            arguments(426, HttpStatus.UPGRADE_REQUIRED),
            arguments(428, HttpStatus.PRECONDITION_REQUIRED),
            arguments(429, HttpStatus.TOO_MANY_REQUESTS),
            arguments(431, HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE),
            arguments(451, HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS),
            arguments(500, HttpStatus.INTERNAL_SERVER_ERROR),
            arguments(501, HttpStatus.NOT_IMPLEMENTED),
            arguments(502, HttpStatus.BAD_GATEWAY),
            arguments(503, HttpStatus.SERVICE_UNAVAILABLE),
            arguments(504, HttpStatus.GATEWAY_TIMEOUT),
            arguments(505, HttpStatus.HTTP_VERSION_NOT_SUPPORTED),
            arguments(506, HttpStatus.VARIANT_ALSO_NEGOTIATES),
            arguments(507, HttpStatus.INSUFFICIENT_STORAGE),
            arguments(508, HttpStatus.LOOP_DETECTED),
            arguments(510, HttpStatus.NOT_EXTENDED),
            arguments(511, HttpStatus.NETWORK_AUTHENTICATION_REQUIRED)
        );
    }

    @ParameterizedTest
    @MethodSource("uncachedStatusProvider")
    void shouldReturnNewStatus(final int status) {
        final var method0 = HttpStatus.valueOf(status);
        final var method1 = HttpStatus.valueOf(status);
        assertNotSame(method0, method1);
        assertEquals(method0, method1);
    }

    static IntStream uncachedStatusProvider() {
        return IntStream.of(
            198,
            250,
            399,
            488,
            578
        );
    }
}
