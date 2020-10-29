package se.arkalix;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import se.arkalix.ServiceInterface;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestServiceInterface {
    @ParameterizedTest
    @MethodSource("badTripletProvider")
    void shouldFailToParseDescriptor(final String descriptor) {
        assertThrows(IllegalArgumentException.class, () -> ServiceInterface.valueOf(descriptor));
    }

    static Stream<String> badTripletProvider() {
        return Stream.of(
            "HTTP/SECURE/JSON",
            "HTTPS-JSON",
            "HTTPS-TLS-JSON",
            "HTTP_SECURE_JSON",
            "MQTT-TLS-CBOR"
        );
    }

    @ParameterizedTest
    @MethodSource("cachedTripletProvider")
    void shouldReturnCachedDescriptor(final String descriptor, final ServiceInterface expected) {
        final var triplet = ServiceInterface.valueOf(descriptor);
        assertSame(expected, triplet);
    }

    static Stream<Arguments> cachedTripletProvider() {
        return Stream.of(
            arguments("AMPQ-SECURE-CBOR", ServiceInterface.AMPQ_SECURE_CBOR),
            arguments("AMPQ-SECURE-JSON", ServiceInterface.AMPQ_SECURE_JSON),
            arguments("AMPQ-SECURE-XML", ServiceInterface.AMPQ_SECURE_XML),
            arguments("AMPQ-SECURE-XSI", ServiceInterface.AMPQ_SECURE_XSI),
            arguments("COAP-SECURE-CBOR", ServiceInterface.COAP_SECURE_CBOR),
            arguments("COAP-SECURE-JSON", ServiceInterface.COAP_SECURE_JSON),
            arguments("COAP-SECURE-XML", ServiceInterface.COAP_SECURE_XML),
            arguments("COAP-SECURE-XSI", ServiceInterface.COAP_SECURE_XSI),
            arguments("HTTP-SECURE-CBOR", ServiceInterface.HTTP_SECURE_CBOR),
            arguments("HTTP-SECURE-JSON", ServiceInterface.HTTP_SECURE_JSON),
            arguments("HTTP-SECURE-XML", ServiceInterface.HTTP_SECURE_XML),
            arguments("HTTP-SECURE-XSI", ServiceInterface.HTTP_SECURE_XSI),
            arguments("MQTT-SECURE-CBOR", ServiceInterface.MQTT_SECURE_CBOR),
            arguments("MQTT-SECURE-JSON", ServiceInterface.MQTT_SECURE_JSON),
            arguments("MQTT-SECURE-XML", ServiceInterface.MQTT_SECURE_XML),
            arguments("MQTT-SECURE-XSI", ServiceInterface.MQTT_SECURE_XSI),
            arguments("XMPP-SECURE-CBOR", ServiceInterface.XMPP_SECURE_CBOR),
            arguments("XMPP-SECURE-JSON", ServiceInterface.XMPP_SECURE_JSON),
            arguments("XMPP-SECURE-XML", ServiceInterface.XMPP_SECURE_XML),
            arguments("XMPP-SECURE-XSI", ServiceInterface.XMPP_SECURE_XSI)
        );
    }

    @ParameterizedTest
    @MethodSource("uncachedTripletProvider")
    void shouldReturnNewDescriptor(final String descriptor) {
        final var triplet0 = ServiceInterface.valueOf(descriptor);
        final var triplet1 = ServiceInterface.valueOf(descriptor);
        assertNotSame(triplet0, triplet1);
        assertEquals(triplet0, triplet1);
    }

    static Stream<String> uncachedTripletProvider() {
        return Stream.of(
            "HTTP-INSECURE-JSON",
            "http-SECURE-BSON",
            "MQTT-insecure-CAPNPROTO",
            "JABBER-SECURE-protobuf",
            "jabber-secure-json"
        );
    }
}
