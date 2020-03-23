package se.arkalix.internal.net.dns;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestDnsNames {
    @ParameterizedTest
    @MethodSource("validDnsNames")
    void shouldBeValid(final String name) {
        assertTrue(DnsNames.isValid(name));
    }

    static Stream<String> validDnsNames() {
        return Stream.of(
            "x",
            "x.y",
            "x.y.z",
            "x1.y2.z3",
            "x--1.y--2.z--3",
            "a1234567.x",
            "arrowhead.eu",
            "company1.arrowhead.eu",
            "cloud-1.company1.arrowhead.eu",
            "system001.cloud-1.company1.arrowhead.eu"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDnsNames")
    void shouldBeInvalid(final String name) {
        assertFalse(DnsNames.isValid(name));
    }

    static Stream<String> invalidDnsNames() {
        return Stream.of(
            ".x",
            "x.",
            "x..y",
            "1",
            ".",
            "1.1",
            "a.1",
            ".x.y",
            "-x.y",
            "x.-y",
            "x-",
            "x-.y",
            "x.y-"
        );
    }
}
