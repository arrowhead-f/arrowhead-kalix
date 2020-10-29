package se.arkalix.net.http._internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.encoding.Encoding;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpMediaTypes {
    @ParameterizedTest
    @MethodSource("matchingContentTypeEncodingArguments")
    void shouldProduceCompatibleEncoding(
        final String contentType,
        final Encoding expected
    ) {
        final var actual = HttpMediaTypes.encodingFromContentType(contentType);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> matchingContentTypeEncodingArguments() {
        return Stream.of(
            arguments("application/json", Encoding.JSON),
            arguments("application/json; charset=utf-8", Encoding.JSON),
            arguments("application/senml-exi", Encoding.EXI),
            arguments("application/jose+json", Encoding.JSON),
            arguments("text/xml; charset=utf-8", Encoding.XML),
            arguments("text/html; charset=utf-16", Encoding.getOrCreate("html")),
            arguments("application/sensml+xml", Encoding.XML),
            arguments("application/senml+cbor", Encoding.CBOR)
        );
    }

    @ParameterizedTest
    @MethodSource("compatibleContentTypeEncodingArguments")
    void shouldFindCompatibleEncoding(
        final String contentType,
        final Encoding[] encodings,
        final Encoding expected
    ) {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithContentType(Arrays.asList(encodings), contentType);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> compatibleContentTypeEncodingArguments() {
        return Stream.of(
            arguments("application/json", new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.JSON),

            arguments("application/json; charset=utf-8", new Encoding[]{
                Encoding.EXI, Encoding.JSON, Encoding.XML
            }, Encoding.JSON),

            arguments("application/senml-exi", new Encoding[]{
                Encoding.EXI, Encoding.JSON, Encoding.XML
            }, Encoding.EXI),

            arguments("application/jose+json", new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.JSON),

            arguments("text/xml; charset=utf-8", new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.XML),

            arguments("application/xml; charset=utf-16", new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.XML),

            arguments("application/sensml+xml", new Encoding[]{
                Encoding.JSON, Encoding.XML, Encoding.CBOR,
            }, Encoding.XML),

            arguments("application/senml+cbor", new Encoding[]{
                Encoding.CBOR, Encoding.XML
            }, Encoding.CBOR)
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleContentTypeEncodingArguments")
    void shouldNotFindCompatibleEncoding(final String contentType, final Encoding[] encodings) {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithContentType(Arrays.asList(encodings), contentType);
        assertFalse(actual.isPresent());
    }

    static Stream<Arguments> incompatibleContentTypeEncodingArguments() {
        return Stream.of(
            arguments("application/json", new Encoding[]{
                Encoding.XML
            }),

            arguments("application/json; charset=utf-8", new Encoding[]{
                Encoding.EXI, Encoding.XML
            }),

            arguments("application/senml-exi", new Encoding[]{
                Encoding.JSON, Encoding.XML
            }),

            arguments("application/jose+json", new Encoding[]{
                Encoding.XML
            }),

            arguments("text/xml", new Encoding[]{
                Encoding.JSON
            }),

            arguments("application/sensml+xml", new Encoding[]{
                Encoding.JSON, Encoding.CBOR,
            }),

            arguments("application/senml+cbor", new Encoding[]{
                Encoding.XML
            })
        );
    }

    @ParameterizedTest
    @MethodSource("compatibleAcceptFieldsEncodingArguments")
    void shouldFindCompatibleEncodingAmongAcceptFields(
        final List<String> headers,
        final Encoding[] encodings,
        final Encoding expected)
    {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(Arrays.asList(encodings), headers);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> compatibleAcceptFieldsEncodingArguments() {
        return Stream.of(
            arguments(Collections.singletonList("*/*"), new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.JSON),

            arguments(Collections.singletonList("*/senml+json"), new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.JSON),

            arguments(Collections.singletonList("application/*"), new Encoding[]{
                Encoding.JSON, Encoding.XML
            }, Encoding.JSON),

            arguments(Collections.singletonList("text/*"), new Encoding[]{
                Encoding.XML, Encoding.JSON
            }, Encoding.XML),

            arguments(Arrays.asList("*/json", "*/cbor"), new Encoding[]{
                Encoding.EXI, Encoding.CBOR
            }, Encoding.CBOR),

            arguments(Arrays.asList("*/json, */senml-exi", "*/cbor"), new Encoding[]{
                Encoding.EXI, Encoding.CBOR
            }, Encoding.EXI),

            arguments(Arrays.asList("*/json;q=1.0", "*/cbor;q=0.9"), new Encoding[]{
                Encoding.EXI, Encoding.CBOR
            }, Encoding.CBOR),

            arguments(Arrays.asList("*/json;q=1.0, */exi;q=0.9", "*/cbor;q=0.8"), new Encoding[]{
                Encoding.EXI, Encoding.CBOR
            }, Encoding.EXI)
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleAcceptFieldsEncodingArguments")
    void shouldNotFindCompatibleEncodingAmongAcceptFields(
        final List<String> headers,
        final Encoding[] encodings)
    {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(Arrays.asList(encodings), headers);
        assertFalse(actual.isPresent());
    }

    static Stream<Arguments> incompatibleAcceptFieldsEncodingArguments() {
        return Stream.of(
            arguments(Collections.singletonList("*/cbor"), new Encoding[]{
                Encoding.JSON, Encoding.XML
            }),

            arguments(Collections.singletonList("application/exi"), new Encoding[]{
                Encoding.JSON, Encoding.XML
            }),
            arguments(Arrays.asList("*/json", "*/cbor"), new Encoding[]{
                Encoding.EXI, Encoding.XML
            }),

            arguments(Arrays.asList("*/json, */exi", "*/cbor"), new Encoding[]{
                Encoding.XML,
            }),

            arguments(Arrays.asList("*/json;q=1.0", "*/cbor;q=0.9"), new Encoding[]{
                Encoding.EXI, Encoding.XML
            }),

            arguments(Arrays.asList("*/json;q=1.0, */exi;q=0.9", "*/cbor;q=0.8"), new Encoding[]{
                Encoding.XML,
            })
        );
    }
}
