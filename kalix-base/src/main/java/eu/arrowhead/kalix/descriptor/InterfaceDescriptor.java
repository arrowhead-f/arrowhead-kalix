package eu.arrowhead.kalix.descriptor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Describes an application message protocol, in terms of its transport
 * protocol, security requirements and message payload encoding.
 */
public class InterfaceDescriptor {
    private static final Pattern TRIPLET_PATTERN = Pattern.compile("^([0-9A-Z_]+)-(IN)?SECURE-([0-9A-Z_]+)$");

    private final TransportDescriptor transport;
    private final boolean isSecure;
    private final EncodingDescriptor encoding;
    private final String text;

    private InterfaceDescriptor(
        final TransportDescriptor transport,
        final boolean isSecure,
        final EncodingDescriptor encoding,
        final String text
    ) {
        this.transport = transport;
        this.isSecure = isSecure;
        this.encoding = encoding;
        this.text = text;
    }

    /**
     * @return Transport protocol descriptor.
     */
    public TransportDescriptor transport() {
        return transport;
    }

    /**
     * @return Whether or not transport security is to be used.
     */
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * @return Message encoding descriptor.
     */
    public EncodingDescriptor encoding() {
        return encoding;
    }

    /**
     * @return Textual description of interface, such as "HTTP-SECURE-JSON".
     */
    public String text() {
        return text;
    }

    /**
     * AMPQ over TLS with ASN.1 payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor AMPQ_SECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.ASN1, "AMPQ-SECURE-ASN1");

    /**
     * AMPQ with ASN.1 payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor AMPQ_INSECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, false, EncodingDescriptor.CBOR, "AMPQ-INSECURE-ASN1");

    /**
     * AMPQ over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor AMPQ_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.CBOR, "AMPQ-SECURE-CBOR");

    /**
     * AMPQ with CBOR payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor AMPQ_INSECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, false, EncodingDescriptor.CBOR, "AMPQ-INSECURE-CBOR");

    /**
     * AMPQ over TLS with JSON payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor AMPQ_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.JSON, "AMPQ-SECURE-JSON");

    /**
     * AMPQ with JSON payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor AMPQ_INSECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, false, EncodingDescriptor.JSON, "AMPQ-INSECURE-JSON");

    /**
     * AMPQ over TLS with XML payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor AMPQ_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.XML, "AMPQ-SECURE-XML");

    /**
     * AMPQ with XML payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor AMPQ_INSECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, false, EncodingDescriptor.XML, "AMPQ-INSECURE-XML");

    /**
     * AMPQ over TLS with XSI payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor AMPQ_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.XSI, "AMPQ-SECURE-XSI");

    /**
     * AMPQ with XSI payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor AMPQ_INSECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, false, EncodingDescriptor.XSI, "AMPQ-INSECURE-XSI");

    /**
     * CoAP over TLS with ASN.1 payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor COAP_SECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.ASN1, "COAP-SECURE-ASN1");

    /**
     * CoAP with ASN.1 payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor COAP_INSECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.COAP, false, EncodingDescriptor.CBOR, "COAP-INSECURE-ASN1");

    /**
     * CoAP over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor COAP_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.CBOR, "COAP-SECURE-CBOR");

    /**
     * CoAP with CBOR payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor COAP_INSECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.COAP, false, EncodingDescriptor.CBOR, "COAP-INSECURE-CBOR");

    /**
     * CoAP over TLS with JSON payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor COAP_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.JSON, "COAP-SECURE-JSON");

    /**
     * CoAP with JSON payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor COAP_INSECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.COAP, false, EncodingDescriptor.JSON, "COAP-INSECURE-JSON");

    /**
     * CoAP over TLS with XML payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor COAP_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.XML, "COAP-SECURE-XML");

    /**
     * CoAP with XML payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor COAP_INSECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.COAP, false, EncodingDescriptor.XML, "COAP-INSECURE-XML");

    /**
     * CoAP over TLS with XSI payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor COAP_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.XSI, "COAP-SECURE-XSI");

    /**
     * CoAP with XSI payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor COAP_INSECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.COAP, false, EncodingDescriptor.XSI, "COAP-INSECURE-XSI");

    /**
     * HTTPS with ASN.1 payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor HTTP_SECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.ASN1, "HTTP-SECURE-ASN1");

    /**
     * Plain HTTP with ASN.1 payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor HTTP_INSECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.HTTP, false, EncodingDescriptor.ASN1, "HTTP-INSECURE-ASN1");

    /**
     * HTTPS with CBOR payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor HTTP_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.CBOR, "HTTP-SECURE-CBOR");

    /**
     * Plain HTTP with CBOR payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor HTTP_INSECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.HTTP, false, EncodingDescriptor.CBOR, "HTTP-INSECURE-CBOR");

    /**
     * HTTPS with JSON payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor HTTP_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.JSON, "HTTP-SECURE-JSON");

    /**
     * Plain HTTP with JSON payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor HTTP_INSECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.HTTP, false, EncodingDescriptor.JSON, "HTTP-INSECURE-JSON");

    /**
     * HTTPS with XML payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor HTTP_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.XML, "HTTP-SECURE-XML");

    /**
     * Plain HTTP with XML payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor HTTP_INSECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.HTTP, false, EncodingDescriptor.XML, "HTTP-INSECURE-XML");

    /**
     * HTTPS with XSI payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor HTTP_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.XSI, "HTTP-SECURE-XSI");

    /**
     * Plain HTTP with XSI payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor HTTP_INSECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.HTTP, false, EncodingDescriptor.XSI, "HTTP-INSECURE-XSI");

    /**
     * MQTT over TLS with ASN.1 payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor MQTT_SECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.ASN1, "MQTT-SECURE-ASN1");

    /**
     * Plain MQTT with ASN.1 payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor MQTT_INSECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.MQTT, false, EncodingDescriptor.ASN1, "MQTT-INSECURE-ASN1");

    /**
     * MQTT over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor MQTT_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.CBOR, "MQTT-SECURE-CBOR");

    /**
     * Plain MQTT with CBOR payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor MQTT_INSECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.MQTT, false, EncodingDescriptor.CBOR, "MQTT-INSECURE-CBOR");

    /**
     * MQTT over TLS with JSON payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor MQTT_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.JSON, "MQTT-SECURE-JSON");

    /**
     * Plain MQTT with JSON payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor MQTT_INSECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.MQTT, false, EncodingDescriptor.JSON, "MQTT-INSECURE-JSON");

    /**
     * MQTT over TLS with XML payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor MQTT_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.XML, "MQTT-SECURE-XML");

    /**
     * Plain MQTT with XML payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor MQTT_INSECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.MQTT, false, EncodingDescriptor.XML, "MQTT-INSECURE-XML");

    /**
     * MQTT over TLS with XSI payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor MQTT_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.XSI, "MQTT-SECURE-XSI");

    /**
     * Plain MQTT with XSI payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor MQTT_INSECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.MQTT, false, EncodingDescriptor.XSI, "MQTT-INSECURE-XSI");

    /**
     * XMPP over TLS with ASN.1 payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor XMPP_SECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.ASN1, "XMPP-SECURE-ASN1");

    /**
     * Plain XMPP with ASN.1 payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#ASN1
     */
    public static final InterfaceDescriptor XMPP_INSECURE_ASN1 = new InterfaceDescriptor(
        TransportDescriptor.XMPP, false, EncodingDescriptor.ASN1, "XMPP-INSECURE-ASN1");

    /**
     * XMPP over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor XMPP_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.CBOR, "XMPP-SECURE-CBOR");

    /**
     * Plain XMPP with CBOR payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor XMPP_INSECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.XMPP, false, EncodingDescriptor.CBOR, "XMPP-INSECURE-CBOR");

    /**
     * XMPP over TLS with JSON payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor XMPP_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.JSON, "XMPP-SECURE-JSON");

    /**
     * Plain XMPP with JSON payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor XMPP_INSECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.XMPP, false, EncodingDescriptor.JSON, "XMPP-INSECURE-JSON");

    /**
     * XMPP over TLS with XML payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor XMPP_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.XML, "XMPP-SECURE-XML");

    /**
     * Plain XMPP with XML payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor XMPP_INSECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.XMPP, false, EncodingDescriptor.XML, "XMPP-INSECURE-XML");

    /**
     * XMPP over TLS with XSI payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor XMPP_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.XSI, "XMPP-SECURE-XSI");

    /**
     * Plain XMPP with XSI payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#XSI
     */
    public static final InterfaceDescriptor XMPP_INSECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.HTTP, false, EncodingDescriptor.XSI, "XMPP-INSECURE-XSI");

    /**
     * Parses given string into interface triplet.
     * <p>
     * Valid strings must match the following regular expression:
     * <pre>
     * ([0-9A-Z_]+)-(IN)?SECURE-([0-9A-Z_]+)
     * </pre>
     *
     * @param triplet String to parse.
     * @return Cached or new interface triplet.
     * @throws IllegalArgumentException If {@code triplet} is malformed.
     */
    public static InterfaceDescriptor valueOf(String triplet) {
        triplet = Objects.requireNonNull(triplet, "Name required").toUpperCase();
        switch (triplet) {
        case "AMPQ-SECURE-ASN1": return AMPQ_SECURE_ASN1;
        case "AMPQ-INSECURE-ASN1": return AMPQ_INSECURE_ASN1;
        case "AMPQ-SECURE-CBOR": return AMPQ_SECURE_CBOR;
        case "AMPQ-INSECURE-CBOR": return AMPQ_INSECURE_CBOR;
        case "AMPQ-SECURE-JSON": return AMPQ_SECURE_JSON;
        case "AMPQ-INSECURE-JSON": return AMPQ_INSECURE_JSON;
        case "AMPQ-SECURE-XML": return AMPQ_SECURE_XML;
        case "AMPQ-INSECURE-XML": return AMPQ_INSECURE_XML;
        case "AMPQ-SECURE-XSI": return AMPQ_SECURE_XSI;
        case "AMPQ-INSECURE-XSI": return AMPQ_INSECURE_XSI;
        case "COAP-SECURE-ASN1": return COAP_SECURE_ASN1;
        case "COAP-INSECURE-ASN1": return COAP_INSECURE_ASN1;
        case "COAP-SECURE-CBOR": return COAP_SECURE_CBOR;
        case "COAP-INSECURE-CBOR": return COAP_INSECURE_CBOR;
        case "COAP-SECURE-JSON": return COAP_SECURE_JSON;
        case "COAP-INSECURE-JSON": return COAP_INSECURE_JSON;
        case "COAP-SECURE-XML": return COAP_SECURE_XML;
        case "COAP-INSECURE-XML": return COAP_INSECURE_XML;
        case "COAP-SECURE-XSI": return COAP_SECURE_XSI;
        case "COAP-INSECURE-XSI": return COAP_INSECURE_XSI;
        case "HTTP-SECURE-ASN1": return HTTP_SECURE_ASN1;
        case "HTTP-INSECURE-ASN1": return HTTP_INSECURE_ASN1;
        case "HTTP-SECURE-CBOR": return HTTP_SECURE_CBOR;
        case "HTTP-INSECURE-CBOR": return HTTP_INSECURE_CBOR;
        case "HTTP-SECURE-JSON": return HTTP_SECURE_JSON;
        case "HTTP-INSECURE-JSON": return HTTP_INSECURE_JSON;
        case "HTTP-SECURE-XML": return HTTP_SECURE_XML;
        case "HTTP-INSECURE-XML": return HTTP_INSECURE_XML;
        case "HTTP-SECURE-XSI": return HTTP_SECURE_XSI;
        case "HTTP-INSECURE-XSI": return HTTP_INSECURE_XSI;
        case "MQTT-SECURE-ASN1": return MQTT_SECURE_ASN1;
        case "MQTT-INSECURE-ASN1": return MQTT_INSECURE_ASN1;
        case "MQTT-SECURE-CBOR": return MQTT_SECURE_CBOR;
        case "MQTT-INSECURE-CBOR": return MQTT_INSECURE_CBOR;
        case "MQTT-SECURE-JSON": return MQTT_SECURE_JSON;
        case "MQTT-INSECURE-JSON": return MQTT_INSECURE_JSON;
        case "MQTT-SECURE-XML": return MQTT_SECURE_XML;
        case "MQTT-INSECURE-XML": return MQTT_INSECURE_XML;
        case "MQTT-SECURE-XSI": return MQTT_SECURE_XSI;
        case "MQTT-INSECURE-XSI": return MQTT_INSECURE_XSI;
        case "XMPP-SECURE-ASN1": return XMPP_SECURE_ASN1;
        case "XMPP-INSECURE-ASN1": return XMPP_INSECURE_ASN1;
        case "XMPP-SECURE-CBOR": return XMPP_SECURE_CBOR;
        case "XMPP-INSECURE-CBOR": return XMPP_INSECURE_CBOR;
        case "XMPP-SECURE-JSON": return XMPP_SECURE_JSON;
        case "XMPP-INSECURE-JSON": return XMPP_INSECURE_JSON;
        case "XMPP-SECURE-XML": return XMPP_SECURE_XML;
        case "XMPP-INSECURE-XML": return XMPP_INSECURE_XML;
        case "XMPP-SECURE-XSI": return XMPP_SECURE_XSI;
        case "XMPP-INSECURE-XSI": return XMPP_INSECURE_XSI;
        }

        final var matcher = TRIPLET_PATTERN.matcher(triplet);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Arrowhead interface string \"" + triplet
                + "\"; must match \"" + TRIPLET_PATTERN + "\"");
        }
        return new InterfaceDescriptor(
            TransportDescriptor.valueOf(matcher.group(1)),
            matcher.group(2) == null,
            EncodingDescriptor.valueOf(matcher.group(3)),
            triplet
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        InterfaceDescriptor triplet = (InterfaceDescriptor) o;
        return transport.equals(triplet.transport) &&
            isSecure == triplet.isSecure &&
            encoding.equals(triplet.encoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transport, isSecure, encoding);
    }

    @Override
    public String toString() {
        return text;
    }
}
