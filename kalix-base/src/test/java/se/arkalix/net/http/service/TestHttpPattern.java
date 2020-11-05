package se.arkalix.net.http.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpPattern {
    @ParameterizedTest
    @MethodSource("matchingPatternPathParameterSets")
    void shouldMatchPattern(final String pattern, final String path, final String[] expectedParameters) {
        final var actualParameters = new ArrayList<String>(0);
        assertTrue(
            HttpPattern.valueOf(pattern).match(path, 0, actualParameters),
            "Pattern `" + pattern + "` does not match path `" + path + "`"
        );
        assertEquals(Arrays.asList(expectedParameters), actualParameters);
    }

    static Stream<Arguments> matchingPatternPathParameterSets() {
        return Stream.of(
            arguments("/", "", new String[0]),
            arguments("/", "/", new String[0]),
            arguments("/hello", "/hello", new String[0]),
            arguments("/order-summaries", "/order-summaries", new String[0]),
            arguments("/some/path", "/some/path", new String[0]),
            arguments("/buyer/order-summaries", "/buyer/order-summaries", new String[0]),
            arguments("/#0", "/hello", new String[]{"hello"}),
            arguments("/#@/#%", "/swirl/percent", new String[]{"swirl", "percent"}),
            arguments("/#A/#B/#C", "/x/y/z", new String[]{"x", "y", "z"}),
            arguments("/#A/#B/#C", "/x/y/z/", new String[]{"x", "y", "z"}),
            arguments("/#A/#B/#C/", "/x/y/z", new String[]{"x", "y", "z"}),
            arguments("/#A/#B/#C/", "/x/y/z/", new String[]{"x", "y", "z"}),
            arguments("/123/#B/@:", "/123/y/@:", new String[]{"y"}),
            arguments("/#A/>", "/x/y/z", new String[]{"x", "/y/z"}),
            arguments("/>", "/x/y/z", new String[]{"/x/y/z"}),
            arguments("/base/#id/thing", "/base//thing", new String[]{""}),
            arguments("//#id/thing", "///thing", new String[]{""}),
            arguments("///thing", "///thing", new String[0]),
            arguments("///", "///", new String[0]),
            arguments("///#", "///maybe", new String[]{"maybe"}),
            arguments("///>", "///hello", new String[]{"/hello"})
        );
    }

    @ParameterizedTest
    @MethodSource("mismatchingPatternPathPairs")
    void shouldNotMatchPattern(final String pattern, final String path) {
        final var actualParameters = new ArrayList<String>(0);
        assertFalse(
            HttpPattern.valueOf(pattern).match(path, 0, actualParameters),
            "Pattern `" + pattern + "` does match path `" + path + "`"
        );
    }

    static Stream<Arguments> mismatchingPatternPathPairs() {
        return Stream.of(
            arguments("/", "/hello"),
            arguments("/hello", "/"),
            arguments("//#1", "/"),
            arguments("/x/>", "/x/"),
            arguments("/#/hello", "/#/hello"),
            arguments("/#id/hello", "/#/hello"),
            arguments("/#id", "/#"),
            arguments("/lo", "/longer"),
            arguments("/longer", "/lo"),
            arguments("/lo/>", "/longer"),
            arguments("/longer/>", "/lo")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPatterns")
    void shouldFailToParsePattern(final String pattern) {
        assertThrows(
            IllegalArgumentException.class, () -> HttpPattern.valueOf(pattern),
            "pattern `" + pattern + "` to be invalid."
        );
    }

    static Stream<String> invalidPatterns() {
        return Stream.of(
            "",
            "hello",
            "hello/",
            "/>/",
            "/hello#",
            "/hello>",
            "/hello,%20world!",
            "/x/y/./z",
            "/x/y/../z",
            "/x/y/.",
            "/x/y/.."
        );
    }

    @ParameterizedTest
    @MethodSource("intersectingPatternPairs")
    void shouldConsiderAToIntersectWithB(final String a, final String b) {
        final var a0 = HttpPattern.valueOf(a);
        final var b0 = HttpPattern.valueOf(b);
        assertTrue(a0.intersectsWith(b0));
    }

    static Stream<Arguments> intersectingPatternPairs() {
        return Stream.of(
            arguments("/", "/"),
            arguments("/hello", "/#/"),
            arguments("/x/y", "/x/>"),
            arguments("//#1", "/>"),
            arguments("/>", "/hello/there"),
            arguments("/x/>", "/x/y/z")
        );
    }

    @ParameterizedTest
    @MethodSource("disjointPatternPairs")
    void shouldConsiderADisjointFromB(final String a, final String b) {
        final var a0 = HttpPattern.valueOf(a);
        final var b0 = HttpPattern.valueOf(b);
        assertFalse(a0.intersectsWith(b0));
    }

    static Stream<Arguments> disjointPatternPairs() {
        return Stream.of(
            arguments("/", "/x"),
            arguments("/hello", "/"),
            arguments("/z/y", "/x/>"),
            arguments("/x/", "/x/y/z")
        );
    }

    @Test
    void shouldSortPatternsCorrectly() {
        final var patterns = Stream.of(
            "/",
            "/hello/morning",
            "/#",
            "/>",
            "/hello",
            "/hello/>",
            "/hello/#name",
            "/#/y/z",
            "/#/y/#",
            "/hello/goodbye",
            "/#x/y/>",
            "/#x/y"
        );

        final var actual = patterns
            .map(HttpPattern::valueOf)
            .sorted()
            .map(HttpPattern::text)
            .toArray(String[]::new);

        final var expected = new String[]{
            "/hello/goodbye",
            "/hello/morning",
            "/hello/#",
            "/hello/>",
            "/hello",
            "/#/y/z",
            "/#/y/#",
            "/#/y/>",
            "/#/y",
            "/#",
            "/",
            "/>",
        };

        assertArrayEquals(expected, actual);
    }
}
