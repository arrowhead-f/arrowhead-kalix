package eu.arrowhead.kalix.description;

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
            arguments("COAP-INSECURE-ASN1", InterfaceDescriptor.COAP_INSECURE_ASN1),
            arguments("COAP-INSECURE-CBOR", InterfaceDescriptor.COAP_INSECURE_CBOR),
            arguments("COAP-INSECURE-JSON", InterfaceDescriptor.COAP_INSECURE_JSON),
            arguments("COAP-INSECURE-XML", InterfaceDescriptor.COAP_INSECURE_XML),
            arguments("COAP-INSECURE-XSI", InterfaceDescriptor.COAP_INSECURE_XSI),
            arguments("COAP-SECURE-ASN1", InterfaceDescriptor.COAP_SECURE_ASN1),
            arguments("COAP-SECURE-CBOR", InterfaceDescriptor.COAP_SECURE_CBOR),
            arguments("COAP-SECURE-JSON", InterfaceDescriptor.COAP_SECURE_JSON),
            arguments("COAP-SECURE-XML", InterfaceDescriptor.COAP_SECURE_XML),
            arguments("COAP-SECURE-XSI", InterfaceDescriptor.COAP_SECURE_XSI),
            arguments("HTTP-INSECURE-ASN1", InterfaceDescriptor.HTTP_INSECURE_ASN1),
            arguments("HTTP-INSECURE-CBOR", InterfaceDescriptor.HTTP_INSECURE_CBOR),
            arguments("HTTP-INSECURE-JSON", InterfaceDescriptor.HTTP_INSECURE_JSON),
            arguments("HTTP-INSECURE-XML", InterfaceDescriptor.HTTP_INSECURE_XML),
            arguments("HTTP-INSECURE-XSI", InterfaceDescriptor.HTTP_INSECURE_XSI),
            arguments("HTTP-SECURE-ASN1", InterfaceDescriptor.HTTP_SECURE_ASN1),
            arguments("HTTP-SECURE-CBOR", InterfaceDescriptor.HTTP_SECURE_CBOR),
            arguments("HTTP-SECURE-JSON", InterfaceDescriptor.HTTP_SECURE_JSON),
            arguments("HTTP-SECURE-XML", InterfaceDescriptor.HTTP_SECURE_XML),
            arguments("HTTP-SECURE-XSI", InterfaceDescriptor.HTTP_SECURE_XSI),
            arguments("MQTT-INSECURE-ASN1", InterfaceDescriptor.MQTT_INSECURE_ASN1),
            arguments("MQTT-INSECURE-CBOR", InterfaceDescriptor.MQTT_INSECURE_CBOR),
            arguments("MQTT-INSECURE-JSON", InterfaceDescriptor.MQTT_INSECURE_JSON),
            arguments("MQTT-INSECURE-XML", InterfaceDescriptor.MQTT_INSECURE_XML),
            arguments("MQTT-INSECURE-XSI", InterfaceDescriptor.MQTT_INSECURE_XSI),
            arguments("MQTT-SECURE-ASN1", InterfaceDescriptor.MQTT_SECURE_ASN1),
            arguments("MQTT-SECURE-CBOR", InterfaceDescriptor.MQTT_SECURE_CBOR),
            arguments("MQTT-SECURE-JSON", InterfaceDescriptor.MQTT_SECURE_JSON),
            arguments("MQTT-SECURE-XML", InterfaceDescriptor.MQTT_SECURE_XML),
            arguments("MQTT-SECURE-XSI", InterfaceDescriptor.MQTT_SECURE_XSI),
            arguments("XMPP-INSECURE-ASN1", InterfaceDescriptor.XMPP_INSECURE_ASN1),
            arguments("XMPP-INSECURE-CBOR", InterfaceDescriptor.XMPP_INSECURE_CBOR),
            arguments("XMPP-INSECURE-JSON", InterfaceDescriptor.XMPP_INSECURE_JSON),
            arguments("XMPP-INSECURE-XML", InterfaceDescriptor.XMPP_INSECURE_XML),
            arguments("XMPP-INSECURE-XSI", InterfaceDescriptor.XMPP_INSECURE_XSI),
            arguments("XMPP-SECURE-ASN1", InterfaceDescriptor.XMPP_SECURE_ASN1),
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
            "http-SECURE-BSON",
            "MQTT-insecure-CAPNPROTO",
            "JABBER-SECURE-protobuf",
            "jabber-secure-json"
        );
    }
}
