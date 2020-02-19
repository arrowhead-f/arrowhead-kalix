package eu.arrowhead.kalix.descriptor;

import eu.arrowhead.kalix.internal.collection.PerfectCache;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Describes an application message protocol, in terms of its transport
 * protocol, security requirements and message payload encoding.
 * <p>
 * Often referred to as <i>"triplet"</i> throughout this library, primarily to
 * avoid conflicts with the Java compiler, which considers {@code interface} a
 * reserved keyword.
 */
public class InterfaceDescriptor {
    private static final Pattern TRIPLET_PATTERN = Pattern.compile("([0-9A-Z_]+)-(IN)?SECURE-([0-9A-Z_]+)");

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

    private static final PerfectCache cache = new PerfectCache(5, 256,
        new PerfectCache.Entry(COAP_SECURE_ASN1.text, COAP_SECURE_ASN1),
        new PerfectCache.Entry(COAP_INSECURE_ASN1.text, COAP_INSECURE_ASN1),
        new PerfectCache.Entry(COAP_SECURE_CBOR.text, COAP_SECURE_CBOR),
        new PerfectCache.Entry(COAP_INSECURE_CBOR.text, COAP_INSECURE_CBOR),
        new PerfectCache.Entry(COAP_SECURE_JSON.text, COAP_SECURE_JSON),
        new PerfectCache.Entry(COAP_INSECURE_JSON.text, COAP_INSECURE_JSON),
        new PerfectCache.Entry(COAP_SECURE_XML.text, COAP_SECURE_XML),
        new PerfectCache.Entry(COAP_INSECURE_XML.text, COAP_INSECURE_XML),
        new PerfectCache.Entry(COAP_SECURE_XSI.text, COAP_SECURE_XSI),
        new PerfectCache.Entry(COAP_INSECURE_XSI.text, COAP_INSECURE_XSI),
        new PerfectCache.Entry(HTTP_SECURE_ASN1.text, HTTP_SECURE_ASN1),
        new PerfectCache.Entry(HTTP_INSECURE_ASN1.text, HTTP_INSECURE_ASN1),
        new PerfectCache.Entry(HTTP_SECURE_CBOR.text, HTTP_SECURE_CBOR),
        new PerfectCache.Entry(HTTP_INSECURE_CBOR.text, HTTP_INSECURE_CBOR),
        new PerfectCache.Entry(HTTP_SECURE_JSON.text, HTTP_SECURE_JSON),
        new PerfectCache.Entry(HTTP_INSECURE_JSON.text, HTTP_INSECURE_JSON),
        new PerfectCache.Entry(HTTP_SECURE_XML.text, HTTP_SECURE_XML),
        new PerfectCache.Entry(HTTP_INSECURE_XML.text, HTTP_INSECURE_XML),
        new PerfectCache.Entry(HTTP_SECURE_XSI.text, HTTP_SECURE_XSI),
        new PerfectCache.Entry(HTTP_INSECURE_XSI.text, HTTP_INSECURE_XSI),
        new PerfectCache.Entry(MQTT_SECURE_ASN1.text, MQTT_SECURE_ASN1),
        new PerfectCache.Entry(MQTT_INSECURE_ASN1.text, MQTT_INSECURE_ASN1),
        new PerfectCache.Entry(MQTT_SECURE_CBOR.text, MQTT_SECURE_CBOR),
        new PerfectCache.Entry(MQTT_INSECURE_CBOR.text, MQTT_INSECURE_CBOR),
        new PerfectCache.Entry(MQTT_SECURE_JSON.text, MQTT_SECURE_JSON),
        new PerfectCache.Entry(MQTT_INSECURE_JSON.text, MQTT_INSECURE_JSON),
        new PerfectCache.Entry(MQTT_SECURE_XML.text, MQTT_SECURE_XML),
        new PerfectCache.Entry(MQTT_INSECURE_XML.text, MQTT_INSECURE_XML),
        new PerfectCache.Entry(MQTT_SECURE_XSI.text, MQTT_SECURE_XSI),
        new PerfectCache.Entry(MQTT_INSECURE_XSI.text, MQTT_INSECURE_XSI),
        new PerfectCache.Entry(XMPP_SECURE_ASN1.text, XMPP_SECURE_ASN1),
        new PerfectCache.Entry(XMPP_INSECURE_ASN1.text, XMPP_INSECURE_ASN1),
        new PerfectCache.Entry(XMPP_SECURE_CBOR.text, XMPP_SECURE_CBOR),
        new PerfectCache.Entry(XMPP_INSECURE_CBOR.text, XMPP_INSECURE_CBOR),
        new PerfectCache.Entry(XMPP_SECURE_JSON.text, XMPP_SECURE_JSON),
        new PerfectCache.Entry(XMPP_INSECURE_JSON.text, XMPP_INSECURE_JSON),
        new PerfectCache.Entry(XMPP_SECURE_XML.text, XMPP_SECURE_XML),
        new PerfectCache.Entry(XMPP_INSECURE_XML.text, XMPP_INSECURE_XML),
        new PerfectCache.Entry(XMPP_SECURE_XSI.text, XMPP_SECURE_XSI),
        new PerfectCache.Entry(XMPP_INSECURE_XSI.text, XMPP_INSECURE_XSI)
    );

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
        final var value = cache.get(triplet);
        if (value != null) {
            return (InterfaceDescriptor) value;
        }

        final var matcher = TRIPLET_PATTERN.matcher(triplet);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Arrowhead interface string `" + triplet
                + "`; must match `([A-Z][0-9A-Z_]*)-(IN)?SECURE-([A-Z][0-9A-Z_]*)`");
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
