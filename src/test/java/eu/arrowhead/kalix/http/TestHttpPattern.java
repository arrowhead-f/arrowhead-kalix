package eu.arrowhead.kalix.http;

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
    @Test
    void shouldMatchRootPattern() {
        final var parameters = new ArrayList<String>(0);
        assertTrue(HttpPattern.valueOf("/").apply("/", parameters));
        assertEquals(0, parameters.size());
    }


    @ParameterizedTest
    @MethodSource("validPatternPathParameterSets")
    void shouldMatchPattern(final String pattern, final String path, final String[] expectedParameters) {
        final var actualParameters = new ArrayList<String>(0);
        assertTrue(
            HttpPattern.valueOf(pattern).apply(path, actualParameters),
            "Pattern `" + pattern + "` does not match path `" + path + "`"
        );
        assertEquals(Arrays.asList(expectedParameters), actualParameters);
    }

    static Stream<Arguments> validPatternPathParameterSets() {
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
            arguments("/#A/>", "/x/y/z", new String[]{"x"})
        );
    }
}
