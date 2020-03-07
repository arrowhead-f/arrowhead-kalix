package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.descriptor.EncodingDescriptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestHttpMediaTypes {
    @ParameterizedTest
    @MethodSource("compatibleContentTypeEncodingArguments")
    void shouldFindCompatibleEncoding(
        final String contentType,
        final EncodingDescriptor[] encodings,
        final EncodingDescriptor expected)
    {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithContentType(encodings, contentType);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> compatibleContentTypeEncodingArguments() {
        return Stream.of(
            arguments("application/json", new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.JSON),

            arguments("application/json; charset=utf-8", new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.JSON),

            arguments("application/senml-exi", new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.EXI),

            arguments("application/jose+json", new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.JSON),

            arguments("application/xml; charset=utf-16", new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.XML),

            arguments("application/sensml+xml", new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML, EncodingDescriptor.CBOR,
            }, EncodingDescriptor.XML),

            arguments("application/senml+cbor", new EncodingDescriptor[]{
                EncodingDescriptor.CBOR, EncodingDescriptor.XML
            }, EncodingDescriptor.CBOR)
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleContentTypeEncodingArguments")
    void shouldNotFindCompatibleEncoding(final String contentType, final EncodingDescriptor[] encodings) {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithContentType(encodings, contentType);
        assertFalse(actual.isPresent());
    }

    static Stream<Arguments> incompatibleContentTypeEncodingArguments() {
        return Stream.of(
            arguments("application/json", new EncodingDescriptor[]{
                EncodingDescriptor.XML
            }),

            arguments("application/json; charset=utf-8", new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.XML
            }),

            arguments("application/senml-exi", new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }),

            arguments("application/jose+json", new EncodingDescriptor[]{
                EncodingDescriptor.XML
            }),

            arguments("text/xml", new EncodingDescriptor[]{
                EncodingDescriptor.JSON
            }),

            arguments("application/sensml+xml", new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.CBOR,
            }),

            arguments("application/senml+cbor", new EncodingDescriptor[]{
                EncodingDescriptor.XML
            })
        );
    }

    @ParameterizedTest
    @MethodSource("compatibleAcceptFieldsEncodingArguments")
    void shouldFindCompatibleEncodingAmongAcceptFields(
        final List<String> acceptFields,
        final EncodingDescriptor[] encodings,
        final EncodingDescriptor expected)
    {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(encodings, acceptFields);
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    static Stream<Arguments> compatibleAcceptFieldsEncodingArguments() {
        return Stream.of(
            arguments(Collections.singletonList("*/*"), new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.JSON),

            arguments(Collections.singletonList("*/senml+json"), new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.JSON),

            arguments(Collections.singletonList("application/*"), new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }, EncodingDescriptor.JSON),

            arguments(Collections.singletonList("application/*"), new EncodingDescriptor[]{
                EncodingDescriptor.XML, EncodingDescriptor.JSON
            }, EncodingDescriptor.XML),

            arguments(Arrays.asList("*/json", "*/cbor"), new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.CBOR
            }, EncodingDescriptor.CBOR),

            arguments(Arrays.asList("*/json, */senml-exi", "*/cbor"), new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.CBOR
            }, EncodingDescriptor.EXI),

            arguments(Arrays.asList("*/json;q=1.0", "*/cbor;q=0.9"), new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.CBOR
            }, EncodingDescriptor.CBOR),

            arguments(Arrays.asList("*/json;q=1.0, */exi;q=0.9", "*/cbor;q=0.8"), new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.CBOR
            }, EncodingDescriptor.EXI)
        );
    }

    @ParameterizedTest
    @MethodSource("incompatibleAcceptFieldsEncodingArguments")
    void shouldNotFindCompatibleEncodingAmongAcceptFields(
        final List<String> acceptFields,
        final EncodingDescriptor[] encodings)
    {
        final var actual = HttpMediaTypes.findEncodingCompatibleWithAcceptHeaders(encodings, acceptFields);
        assertFalse(actual.isPresent());
    }

    static Stream<Arguments> incompatibleAcceptFieldsEncodingArguments() {
        return Stream.of(
            arguments(Collections.singletonList("*/cbor"), new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }),

            arguments(Collections.singletonList("application/exi"), new EncodingDescriptor[]{
                EncodingDescriptor.JSON, EncodingDescriptor.XML
            }),
            arguments(Arrays.asList("*/json", "*/cbor"), new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.XML
            }),

            arguments(Arrays.asList("*/json, */exi", "*/cbor"), new EncodingDescriptor[]{
                EncodingDescriptor.XML,
            }),

            arguments(Arrays.asList("*/json;q=1.0", "*/cbor;q=0.9"), new EncodingDescriptor[]{
                EncodingDescriptor.EXI, EncodingDescriptor.XML
            }),

            arguments(Arrays.asList("*/json;q=1.0, */exi;q=0.9", "*/cbor;q=0.8"), new EncodingDescriptor[]{
                EncodingDescriptor.XML,
            })
        );
    }
}
