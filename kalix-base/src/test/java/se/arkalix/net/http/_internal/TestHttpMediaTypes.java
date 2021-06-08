package se.arkalix.net.http._internal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.codec.CodecType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpMediaTypes {
    @ParameterizedTest
    @MethodSource("compatibleContentTypeCodecArguments")
    void shouldFindCompatibleCodec(
        final String contentType,
        final CodecType[] codecTypes,
        final CodecType expected
    ) {
        final var actual = HttpMediaTypes.findCodecTypeCompatibleWithContentType(Arrays.asList(codecTypes), contentType);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> compatibleContentTypeCodecArguments() {
        return Stream.of(
            arguments("application/json", new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.JSON),

            arguments("application/json; charset=utf-8", new CodecType[]{
                CodecType.EXI, CodecType.JSON, CodecType.XML
            }, CodecType.JSON),

            arguments("application/senml-exi", new CodecType[]{
                CodecType.EXI, CodecType.JSON, CodecType.XML
            }, CodecType.EXI),

            arguments("application/jose+json", new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.JSON),

            arguments("text/xml; charset=utf-8", new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.XML),

            arguments("application/xml; charset=utf-16", new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.XML),

            arguments("application/sensml+xml", new CodecType[]{
                CodecType.JSON, CodecType.XML, CodecType.CBOR,
            }, CodecType.XML),

            arguments("application/senml+cbor", new CodecType[]{
                CodecType.CBOR, CodecType.XML
            }, CodecType.CBOR)
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleContentTypeCodecArguments")
    void shouldNotFindCompatibleCodec(final String contentType, final CodecType[] codecTypes) {
        final var actual = HttpMediaTypes.findCodecTypeCompatibleWithContentType(Arrays.asList(codecTypes), contentType);
        assertFalse(actual.isPresent());
    }

    static Stream<Arguments> incompatibleContentTypeCodecArguments() {
        return Stream.of(
            arguments("application/json", new CodecType[]{
                CodecType.XML
            }),

            arguments("application/json; charset=utf-8", new CodecType[]{
                CodecType.EXI, CodecType.XML
            }),

            arguments("application/senml-exi", new CodecType[]{
                CodecType.JSON, CodecType.XML
            }),

            arguments("application/jose+json", new CodecType[]{
                CodecType.XML
            }),

            arguments("text/xml", new CodecType[]{
                CodecType.JSON
            }),

            arguments("application/sensml+xml", new CodecType[]{
                CodecType.JSON, CodecType.CBOR,
            }),

            arguments("application/senml+cbor", new CodecType[]{
                CodecType.XML
            })
        );
    }

    @ParameterizedTest
    @MethodSource("compatibleAcceptFieldsCodecArguments")
    void shouldFindCompatibleCodecAmongAcceptFields(
        final List<String> headers,
        final CodecType[] codecTypes,
        final CodecType expected)
    {
        final var actual = HttpMediaTypes.findCodecTypeCompatibleWithAcceptHeaders(Arrays.asList(codecTypes), headers);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> compatibleAcceptFieldsCodecArguments() {
        return Stream.of(
            arguments(Collections.singletonList("*/*"), new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.JSON),

            arguments(Collections.singletonList("*/senml+json"), new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.JSON),

            arguments(Collections.singletonList("application/*"), new CodecType[]{
                CodecType.JSON, CodecType.XML
            }, CodecType.JSON),

            arguments(Collections.singletonList("text/*"), new CodecType[]{
                CodecType.XML, CodecType.JSON
            }, CodecType.XML),

            arguments(Arrays.asList("*/json", "*/cbor"), new CodecType[]{
                CodecType.EXI, CodecType.CBOR
            }, CodecType.CBOR),

            arguments(Arrays.asList("*/json, */senml-exi", "*/cbor"), new CodecType[]{
                CodecType.EXI, CodecType.CBOR
            }, CodecType.EXI),

            arguments(Arrays.asList("*/json;q=1.0", "*/cbor;q=0.9"), new CodecType[]{
                CodecType.EXI, CodecType.CBOR
            }, CodecType.CBOR),

            arguments(Arrays.asList("*/json;q=1.0, */exi;q=0.9", "*/cbor;q=0.8"), new CodecType[]{
                CodecType.EXI, CodecType.CBOR
            }, CodecType.EXI)
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleAcceptFieldsCodecArguments")
    void shouldNotFindCompatibleCodecAmongAcceptFields(
        final List<String> headers,
        final CodecType[] codecTypes
    )
    {
        final var actual = HttpMediaTypes.findCodecTypeCompatibleWithAcceptHeaders(Arrays.asList(codecTypes), headers);
        assertFalse(actual.isPresent());
    }

    static Stream<Arguments> incompatibleAcceptFieldsCodecArguments() {
        return Stream.of(
            arguments(Collections.singletonList("*/cbor"), new CodecType[]{
                CodecType.JSON, CodecType.XML
            }),

            arguments(Collections.singletonList("application/exi"), new CodecType[]{
                CodecType.JSON, CodecType.XML
            }),
            arguments(Arrays.asList("*/json", "*/cbor"), new CodecType[]{
                CodecType.EXI, CodecType.XML
            }),

            arguments(Arrays.asList("*/json, */exi", "*/cbor"), new CodecType[]{
                CodecType.XML,
            }),

            arguments(Arrays.asList("*/json;q=1.0", "*/cbor;q=0.9"), new CodecType[]{
                CodecType.EXI, CodecType.XML
            }),

            arguments(Arrays.asList("*/json;q=1.0, */exi;q=0.9", "*/cbor;q=0.8"), new CodecType[]{
                CodecType.XML,
            })
        );
    }
}
