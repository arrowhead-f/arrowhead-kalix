package se.arkalix.descriptor;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Names a network interface protocol triplet.
 *
 * Such a triplet names a transport protocol, indicates whether or not TLS is
 * required, as well as naming a message payload encoding.
 */
public final class InterfaceDescriptor implements Comparable<InterfaceDescriptor> {
    private static final Pattern TRIPLET_PATTERN = Pattern.compile("^([0-9A-Z_]+)-(IN)?SECURE-([0-9A-Z_]+)$");

    private static final HashMap<TransportDescriptor, List<InterfaceDescriptor>> CACHE;

    static {
        CACHE = new HashMap<>();
        try {
            for (final var field : InterfaceDescriptor.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == TransportDescriptor.class) {
                    final var descriptor = (InterfaceDescriptor) field.get(null);
                    CACHE.compute(descriptor.transport, (key, value) -> {
                        if (value == null) {
                            value = new ArrayList<>();
                        }
                        value.add(descriptor);
                        return value;
                    });
                }
            }
        }
        catch (final Exception exception) {
            throw new RuntimeException("Interface cache initialization failed", exception);
        }
    }

    private final TransportDescriptor transport;
    private final boolean isSecure;
    private final EncodingDescriptor encoding;
    private final String text;

    private InterfaceDescriptor(
        final TransportDescriptor transport,
        final boolean isSecure,
        final EncodingDescriptor encoding,
        final String text)
    {
        this.transport = transport;
        this.isSecure = isSecure;
        this.encoding = encoding;
        this.text = text;
    }

    /**
     * Either acquires a cached interface descriptor matching the given
     * arguments, or uses them to create a new descriptor.
     *
     * @param transport Transport descriptor.
     * @param isSecure  Whether transport security is to be used.
     * @param encoding  Encoding descriptor.
     * @return New or existing interface descriptor.
     */
    public static InterfaceDescriptor getOrCreate(
        final TransportDescriptor transport,
        final boolean isSecure,
        final EncodingDescriptor encoding)
    {
        if (isSecure) {
            final var candidates = CACHE.get(transport);
            if (candidates != null) {
                for (final var candidate : candidates) {
                    if (candidate.encoding == encoding) {
                        return candidate;
                    }
                }
            }
        }
        return new InterfaceDescriptor(transport, isSecure, encoding,
            transport + (isSecure ? "-SECURE-" : "-INSECURE-") + encoding);
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
     * AMPQ over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor AMPQ_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.CBOR, "AMPQ-SECURE-CBOR");

    /**
     * AMPQ over TLS with JSON payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor AMPQ_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.JSON, "AMPQ-SECURE-JSON");

    /**
     * AMPQ over TLS with XML payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor AMPQ_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.XML, "AMPQ-SECURE-XML");

    /**
     * AMPQ over TLS with XSI payloads.
     *
     * @see TransportDescriptor#AMPQ
     * @see EncodingDescriptor#EXI
     */
    public static final InterfaceDescriptor AMPQ_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.AMPQ, true, EncodingDescriptor.EXI, "AMPQ-SECURE-XSI");

    /**
     * CoAP over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor COAP_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.CBOR, "COAP-SECURE-CBOR");

    /**
     * CoAP over TLS with JSON payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor COAP_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.JSON, "COAP-SECURE-JSON");

    /**
     * CoAP over TLS with XML payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor COAP_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.XML, "COAP-SECURE-XML");

    /**
     * CoAP over TLS with XSI payloads.
     *
     * @see TransportDescriptor#COAP
     * @see EncodingDescriptor#EXI
     */
    public static final InterfaceDescriptor COAP_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.COAP, true, EncodingDescriptor.EXI, "COAP-SECURE-XSI");

    /**
     * HTTPS with CBOR payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor HTTP_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.CBOR, "HTTP-SECURE-CBOR");

    /**
     * HTTPS with JSON payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor HTTP_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.JSON, "HTTP-SECURE-JSON");

    /**
     * HTTPS with XML payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor HTTP_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.XML, "HTTP-SECURE-XML");

    /**
     * HTTPS with XSI payloads.
     *
     * @see TransportDescriptor#HTTP
     * @see EncodingDescriptor#EXI
     */
    public static final InterfaceDescriptor HTTP_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.HTTP, true, EncodingDescriptor.EXI, "HTTP-SECURE-XSI");

    /**
     * MQTT over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor MQTT_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.CBOR, "MQTT-SECURE-CBOR");
    /**
     * MQTT over TLS with JSON payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor MQTT_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.JSON, "MQTT-SECURE-JSON");

    /**
     * MQTT over TLS with XML payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor MQTT_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.XML, "MQTT-SECURE-XML");

    /**
     * MQTT over TLS with XSI payloads.
     *
     * @see TransportDescriptor#MQTT
     * @see EncodingDescriptor#EXI
     */
    public static final InterfaceDescriptor MQTT_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.MQTT, true, EncodingDescriptor.EXI, "MQTT-SECURE-XSI");

    /**
     * XMPP over TLS with CBOR payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#CBOR
     */
    public static final InterfaceDescriptor XMPP_SECURE_CBOR = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.CBOR, "XMPP-SECURE-CBOR");

    /**
     * XMPP over TLS with JSON payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#JSON
     */
    public static final InterfaceDescriptor XMPP_SECURE_JSON = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.JSON, "XMPP-SECURE-JSON");

    /**
     * XMPP over TLS with XML payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#XML
     */
    public static final InterfaceDescriptor XMPP_SECURE_XML = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.XML, "XMPP-SECURE-XML");

    /**
     * XMPP over TLS with XSI payloads.
     *
     * @see TransportDescriptor#XMPP
     * @see EncodingDescriptor#EXI
     */
    public static final InterfaceDescriptor XMPP_SECURE_XSI = new InterfaceDescriptor(
        TransportDescriptor.XMPP, true, EncodingDescriptor.EXI, "XMPP-SECURE-XSI");

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
        case "AMPQ-SECURE-CBOR": return AMPQ_SECURE_CBOR;
        case "AMPQ-SECURE-JSON": return AMPQ_SECURE_JSON;
        case "AMPQ-SECURE-XML": return AMPQ_SECURE_XML;
        case "AMPQ-SECURE-XSI": return AMPQ_SECURE_XSI;
        case "COAP-SECURE-CBOR": return COAP_SECURE_CBOR;
        case "COAP-SECURE-JSON": return COAP_SECURE_JSON;
        case "COAP-SECURE-XML": return COAP_SECURE_XML;
        case "COAP-SECURE-XSI": return COAP_SECURE_XSI;
        case "HTTP-SECURE-CBOR": return HTTP_SECURE_CBOR;
        case "HTTP-SECURE-JSON": return HTTP_SECURE_JSON;
        case "HTTP-SECURE-XML": return HTTP_SECURE_XML;
        case "HTTP-SECURE-XSI": return HTTP_SECURE_XSI;
        case "MQTT-SECURE-CBOR": return MQTT_SECURE_CBOR;
        case "MQTT-SECURE-JSON": return MQTT_SECURE_JSON;
        case "MQTT-SECURE-XML": return MQTT_SECURE_XML;
        case "MQTT-SECURE-XSI": return MQTT_SECURE_XSI;
        case "XMPP-SECURE-CBOR": return XMPP_SECURE_CBOR;
        case "XMPP-SECURE-JSON": return XMPP_SECURE_JSON;
        case "XMPP-SECURE-XML": return XMPP_SECURE_XML;
        case "XMPP-SECURE-XSI": return XMPP_SECURE_XSI;
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
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final var triplet = (InterfaceDescriptor) other;
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

    @Override
    public int compareTo(final InterfaceDescriptor other) {
        return text.compareTo(other.text);
    }
}
