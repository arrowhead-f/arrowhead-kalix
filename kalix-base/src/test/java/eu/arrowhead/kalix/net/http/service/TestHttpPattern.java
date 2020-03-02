package eu.arrowhead.kalix.net.http.service;

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
            HttpPattern.valueOf(pattern).match(path, actualParameters),
            "Pattern `" + pattern + "` does not match path `" + path + "`"
        );
        assertEquals(Arrays.asList(expectedParameters), actualParameters);
    }

    static Stream<Arguments> matchingPatternPathParameterSets() {
        return Stream.of(
            arguments("/", "/", new String[0]),
            arguments("/hello", "/hello", new String[0]),
            arguments("/some/path", "/some/path", new String[0]),
            arguments("/#0", "/hello", new String[]{"hello"}),
            arguments("/#@/#%", "/swirl/percent", new String[]{"swirl", "percent"}),
            arguments("/#A/#B/#C", "/x/y/z", new String[]{"x", "y", "z"}),
            arguments("/#A/#B/#C", "/x/y/z/", new String[]{"x", "y", "z"}),
            arguments("/#A/#B/#C/", "/x/y/z", new String[]{"x", "y", "z"}),
            arguments("/#A/#B/#C/", "/x/y/z/", new String[]{"x", "y", "z"}),
            arguments("/123/#B/@:", "/123/y/@:", new String[]{"y"}),
            arguments("/#A/>", "/x/y/z", new String[]{"x"}),
            arguments("/>", "/x/y/z", new String[0]),
            arguments("/base/#id/thing", "/base//thing", new String[]{""}),
            arguments("//#id/thing", "///thing", new String[]{""}),
            arguments("///thing", "///thing", new String[0]),
            arguments("///", "///", new String[0]),
            arguments("///#", "///maybe", new String[]{"maybe"}),
            arguments("///>", "///hello", new String[0])
        );
    }

    @ParameterizedTest
    @MethodSource("mismatchingPatternPathPairs")
    void shouldNotMatchPattern(final String pattern, final String path) {
        final var actualParameters = new ArrayList<String>(0);
        assertFalse(
            HttpPattern.valueOf(pattern).match(path, actualParameters),
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
            arguments("/#id", "/#")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidPatterns")
    void shouldFailToParsePattern(final String pattern) {
        assertThrows(
            IllegalArgumentException.class, () -> HttpPattern.valueOf(pattern),
            "Expected pattern `" + pattern + "` to be invalid."
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
}
