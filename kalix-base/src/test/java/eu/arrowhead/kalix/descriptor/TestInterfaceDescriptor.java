package eu.arrowhead.kalix.descriptor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class TestInterfaceDescriptor {
    @ParameterizedTest
    @MethodSource("badTripletProvider")
    void shouldFailToParseDescriptor(final String descriptor) {
        assertThrows(IllegalArgumentException.class, () -> InterfaceDescriptor.valueOf(descriptor));
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
    void shouldReturnCachedDescriptor(final String descriptor, final InterfaceDescriptor expected) {
        final var triplet = InterfaceDescriptor.valueOf(descriptor);
        assertSame(expected, triplet);
    }

    static Stream<Arguments> cachedTripletProvider() {
        return Stream.of(
            arguments("AMPQ-SECURE-CBOR", InterfaceDescriptor.AMPQ_SECURE_CBOR),
            arguments("AMPQ-SECURE-JSON", InterfaceDescriptor.AMPQ_SECURE_JSON),
            arguments("AMPQ-SECURE-XML", InterfaceDescriptor.AMPQ_SECURE_XML),
            arguments("AMPQ-SECURE-XSI", InterfaceDescriptor.AMPQ_SECURE_XSI),
            arguments("COAP-SECURE-CBOR", InterfaceDescriptor.COAP_SECURE_CBOR),
            arguments("COAP-SECURE-JSON", InterfaceDescriptor.COAP_SECURE_JSON),
            arguments("COAP-SECURE-XML", InterfaceDescriptor.COAP_SECURE_XML),
            arguments("COAP-SECURE-XSI", InterfaceDescriptor.COAP_SECURE_XSI),
            arguments("HTTP-SECURE-CBOR", InterfaceDescriptor.HTTP_SECURE_CBOR),
            arguments("HTTP-SECURE-JSON", InterfaceDescriptor.HTTP_SECURE_JSON),
            arguments("HTTP-SECURE-XML", InterfaceDescriptor.HTTP_SECURE_XML),
            arguments("HTTP-SECURE-XSI", InterfaceDescriptor.HTTP_SECURE_XSI),
            arguments("MQTT-SECURE-CBOR", InterfaceDescriptor.MQTT_SECURE_CBOR),
            arguments("MQTT-SECURE-JSON", InterfaceDescriptor.MQTT_SECURE_JSON),
            arguments("MQTT-SECURE-XML", InterfaceDescriptor.MQTT_SECURE_XML),
            arguments("MQTT-SECURE-XSI", InterfaceDescriptor.MQTT_SECURE_XSI),
            arguments("XMPP-SECURE-CBOR", InterfaceDescriptor.XMPP_SECURE_CBOR),
            arguments("XMPP-SECURE-JSON", InterfaceDescriptor.XMPP_SECURE_JSON),
            arguments("XMPP-SECURE-XML", InterfaceDescriptor.XMPP_SECURE_XML),
            arguments("XMPP-SECURE-XSI", InterfaceDescriptor.XMPP_SECURE_XSI)
        );
    }

    @ParameterizedTest
    @MethodSource("uncachedTripletProvider")
    void shouldReturnNewDescriptor(final String descriptor) {
        final var triplet0 = InterfaceDescriptor.valueOf(descriptor);
        final var triplet1 = InterfaceDescriptor.valueOf(descriptor);
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
