package se.arkalix.internal.net.dns;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestDnsNames {
    @ParameterizedTest
    @MethodSource("validDnsLabelsAndFirstLabels")
    void shouldHaveValidFirstLabel(final String expected, final String name) {
        assertEquals(expected, DnsNames.firstLabelOf(name));
    }

    static Stream<Arguments> validDnsLabelsAndFirstLabels() {
        return Stream.of(
            arguments("x", "x"),
            arguments("x", "x.y"),
            arguments("x0", "x0.1invalid"),
            arguments("x-0", "x-0"),
            arguments("x--y", "x--y.-invalid-"),
            arguments("x---0", "x---0.y.z")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDnsFirstLabels")
    void shouldHaveInvalidFirstLabel(final String name) {
        assertFalse(DnsNames.isLabel(name));
    }

    static Stream<String> invalidDnsFirstLabels() {
        return Stream.of(
            "1",
            ".",
            "-",
            "x-",
            "-x",
            "-x-",
            "1.y",
            ".y",
            "-.y",
            "x-.y",
            "-x.y",
            "-x-.y"
        );
    }

    @ParameterizedTest
    @MethodSource("validDnsLabels")
    void shouldBeValidLabel(final String name) {
        assertTrue(DnsNames.isLabel(name));
    }

    static Stream<String> validDnsLabels() {
        return Stream.of(
            "x",
            "x0",
            "x-0",
            "x--y",
            "x---0"
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDnsLabels")
    void shouldBeInvalidLabel(final String name) {
        assertFalse(DnsNames.isLabel(name));
    }

    static Stream<String> invalidDnsLabels() {
        return Stream.of(
            "x.y",
            "1",
            ".",
            "-",
            "x-",
            "-x",
            "-x-"
        );
    }

    @ParameterizedTest
    @MethodSource("validDnsNames")
    void shouldBeValidName(final String name) {
        assertTrue(DnsNames.isName(name));
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
    void shouldBeInvalidName(final String name) {
        assertFalse(DnsNames.isName(name));
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

    @ParameterizedTest
    @MethodSource("dnsNamesAndSplits")
    void shouldSplitNamesCorrectly(final String name, final int limit, final List<String> expected) {
        final var parts = DnsNames.splitName(name, limit);
        assertEquals(expected, parts);
    }

    static Stream<Arguments> dnsNamesAndSplits() {
        return Stream.of(
            arguments("x", 1, List.of("x")),
            arguments("x.y", 1, List.of("x.y")),
            arguments("x.y", 2, List.of("x", "y")),
            arguments("x.y", 3, List.of("x", "y")),
            arguments("test.arkalix.se", 2, List.of("test", "arkalix.se")),
            arguments("system-94.cloud-14.company-x.arrowhead.eu", 4,
                List.of("system-94", "cloud-14", "company-x", "arrowhead.eu"))
        );
    }
}
