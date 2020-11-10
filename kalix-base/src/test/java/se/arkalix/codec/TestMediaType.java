package se.arkalix.codec;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestMediaType {
    @ParameterizedTest
    @MethodSource("valuesToParse")
    void shouldParse(final MediaType expected, final String input) {
        final var actual = MediaType.valueOf(input);
        assertEquals(expected, actual);
    }

    static Stream<Arguments> valuesToParse() {
        return Stream.of(
            arguments(MediaType.APPLICATION_CBOR, "application/cbor"),
            arguments(MediaType.APPLICATION_JSON, "application/json"),
            arguments(MediaType.APPLICATION_XML, "application/xml"),
            arguments(MediaType.TEXT_HTML, "text/html"),
            arguments(MediaType.TEXT_PLAIN_ISO_8859_1, "text/plain;charset=iso-8859-1"),
            arguments(MediaType.TEXT_PLAIN_UTF_8, "text/plain;charset=utf-8"),

            arguments(new MediaType(
                "application/json;charset=UTF-8",
                "application",
                "json",
                null,
                Map.of("charset", "UTF-8"),
                CodecType.JSON),
                "APPLICATION/JSON  ;  charSET=UTF-8"
            ),

            arguments(new MediaType(
                "text/xml;charset=utf-16;another=x",
                "text",
                "xml",
                null,
                Map.of("charset", "utf-16", "another", "x"),
                CodecType.XML),
                "text/xml ; charset = utf-16 ; another = x"
            ),

            arguments(new MediaType(
                "application/senml+cbor",
                "application",
                "senml",
                "cbor",
                Map.of(),
                CodecType.CBOR),
                "application/senml+cbor"
            ),

            arguments(new MediaType(
                    "application/calendar+json;charset=us-ascii;component=VTODO",
                    "application",
                    "calendar",
                    "json",
                    Map.of("charset", "us-ascii", "component", "VTODO"),
                    CodecType.JSON),
                "application/calendar+json; charset=us-ascii; component=VTODO"
            )
        );
    }
}
