package se.arkalix;

import se.arkalix.encoding.Encoding;
import se.arkalix.net.Transport;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Names a network interface protocol triplet.
 * <p>
 * Each provided Arrowhead {@link se.arkalix.ArService service} exposes its
 * functionality via at least one <i>interface</i>. An interface consists of a
 * {@link Transport transport protocol}, a requirement to either use
 * or not to use <a href="https://tools.ietf.org/html/rfc7925">TLS/DTLS</a>, as
 * well as a {@link Encoding payload encoding}. When it is advertised what
 * interfaces a certain service supports, each of those interfaces will be
 * represented by an instance of this descriptor.
 */
@SuppressWarnings("unused")
public final class ServiceInterface implements Comparable<ServiceInterface> {
    private static final Pattern TRIPLET_PATTERN = Pattern.compile("^([0-9A-Z_]+)-(IN)?SECURE-([0-9A-Z_]+)$");

    private static final HashMap<Transport, List<ServiceInterface>> CACHE;

    private final Transport transport;
    private final boolean isSecure;
    private final Encoding encoding;
    private final String text;

    private ServiceInterface(
        final Transport transport,
        final boolean isSecure,
        final Encoding encoding,
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
    public static ServiceInterface getOrCreate(
        final Transport transport,
        final boolean isSecure,
        final Encoding encoding)
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
        return new ServiceInterface(transport, isSecure, encoding,
            transport + (isSecure ? "-SECURE-" : "-INSECURE-") + encoding);
    }

    /**
     * @return Transport protocol descriptor.
     */
    public Transport transport() {
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
    public Encoding encoding() {
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
     * @see Transport#AMPQ
     * @see Encoding#CBOR
     */
    public static final ServiceInterface AMPQ_SECURE_CBOR = new ServiceInterface(
        Transport.AMPQ, true, Encoding.CBOR, "AMPQ-SECURE-CBOR");

    /**
     * AMPQ over TLS with JSON payloads.
     *
     * @see Transport#AMPQ
     * @see Encoding#JSON
     */
    public static final ServiceInterface AMPQ_SECURE_JSON = new ServiceInterface(
        Transport.AMPQ, true, Encoding.JSON, "AMPQ-SECURE-JSON");

    /**
     * AMPQ over TLS with XML payloads.
     *
     * @see Transport#AMPQ
     * @see Encoding#XML
     */
    public static final ServiceInterface AMPQ_SECURE_XML = new ServiceInterface(
        Transport.AMPQ, true, Encoding.XML, "AMPQ-SECURE-XML");

    /**
     * AMPQ over TLS with XSI payloads.
     *
     * @see Transport#AMPQ
     * @see Encoding#EXI
     */
    public static final ServiceInterface AMPQ_SECURE_XSI = new ServiceInterface(
        Transport.AMPQ, true, Encoding.EXI, "AMPQ-SECURE-XSI");

    /**
     * CoAP over TLS with CBOR payloads.
     *
     * @see Transport#COAP
     * @see Encoding#CBOR
     */
    public static final ServiceInterface COAP_SECURE_CBOR = new ServiceInterface(
        Transport.COAP, true, Encoding.CBOR, "COAP-SECURE-CBOR");

    /**
     * CoAP over TLS with JSON payloads.
     *
     * @see Transport#COAP
     * @see Encoding#JSON
     */
    public static final ServiceInterface COAP_SECURE_JSON = new ServiceInterface(
        Transport.COAP, true, Encoding.JSON, "COAP-SECURE-JSON");

    /**
     * CoAP over TLS with XML payloads.
     *
     * @see Transport#COAP
     * @see Encoding#XML
     */
    public static final ServiceInterface COAP_SECURE_XML = new ServiceInterface(
        Transport.COAP, true, Encoding.XML, "COAP-SECURE-XML");

    /**
     * CoAP over TLS with XSI payloads.
     *
     * @see Transport#COAP
     * @see Encoding#EXI
     */
    public static final ServiceInterface COAP_SECURE_XSI = new ServiceInterface(
        Transport.COAP, true, Encoding.EXI, "COAP-SECURE-XSI");

    /**
     * HTTPS with CBOR payloads.
     *
     * @see Transport#HTTP
     * @see Encoding#CBOR
     */
    public static final ServiceInterface HTTP_SECURE_CBOR = new ServiceInterface(
        Transport.HTTP, true, Encoding.CBOR, "HTTP-SECURE-CBOR");

    /**
     * HTTPS with JSON payloads.
     *
     * @see Transport#HTTP
     * @see Encoding#JSON
     */
    public static final ServiceInterface HTTP_SECURE_JSON = new ServiceInterface(
        Transport.HTTP, true, Encoding.JSON, "HTTP-SECURE-JSON");

    /**
     * HTTPS with XML payloads.
     *
     * @see Transport#HTTP
     * @see Encoding#XML
     */
    public static final ServiceInterface HTTP_SECURE_XML = new ServiceInterface(
        Transport.HTTP, true, Encoding.XML, "HTTP-SECURE-XML");

    /**
     * HTTPS with XSI payloads.
     *
     * @see Transport#HTTP
     * @see Encoding#EXI
     */
    public static final ServiceInterface HTTP_SECURE_XSI = new ServiceInterface(
        Transport.HTTP, true, Encoding.EXI, "HTTP-SECURE-XSI");

    /**
     * MQTT over TLS with CBOR payloads.
     *
     * @see Transport#MQTT
     * @see Encoding#CBOR
     */
    public static final ServiceInterface MQTT_SECURE_CBOR = new ServiceInterface(
        Transport.MQTT, true, Encoding.CBOR, "MQTT-SECURE-CBOR");
    /**
     * MQTT over TLS with JSON payloads.
     *
     * @see Transport#MQTT
     * @see Encoding#JSON
     */
    public static final ServiceInterface MQTT_SECURE_JSON = new ServiceInterface(
        Transport.MQTT, true, Encoding.JSON, "MQTT-SECURE-JSON");

    /**
     * MQTT over TLS with XML payloads.
     *
     * @see Transport#MQTT
     * @see Encoding#XML
     */
    public static final ServiceInterface MQTT_SECURE_XML = new ServiceInterface(
        Transport.MQTT, true, Encoding.XML, "MQTT-SECURE-XML");

    /**
     * MQTT over TLS with XSI payloads.
     *
     * @see Transport#MQTT
     * @see Encoding#EXI
     */
    public static final ServiceInterface MQTT_SECURE_XSI = new ServiceInterface(
        Transport.MQTT, true, Encoding.EXI, "MQTT-SECURE-XSI");

    /**
     * XMPP over TLS with CBOR payloads.
     *
     * @see Transport#XMPP
     * @see Encoding#CBOR
     */
    public static final ServiceInterface XMPP_SECURE_CBOR = new ServiceInterface(
        Transport.XMPP, true, Encoding.CBOR, "XMPP-SECURE-CBOR");

    /**
     * XMPP over TLS with JSON payloads.
     *
     * @see Transport#XMPP
     * @see Encoding#JSON
     */
    public static final ServiceInterface XMPP_SECURE_JSON = new ServiceInterface(
        Transport.XMPP, true, Encoding.JSON, "XMPP-SECURE-JSON");

    /**
     * XMPP over TLS with XML payloads.
     *
     * @see Transport#XMPP
     * @see Encoding#XML
     */
    public static final ServiceInterface XMPP_SECURE_XML = new ServiceInterface(
        Transport.XMPP, true, Encoding.XML, "XMPP-SECURE-XML");

    /**
     * XMPP over TLS with XSI payloads.
     *
     * @see Transport#XMPP
     * @see Encoding#EXI
     */
    public static final ServiceInterface XMPP_SECURE_XSI = new ServiceInterface(
        Transport.XMPP, true, Encoding.EXI, "XMPP-SECURE-XSI");

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
    public static ServiceInterface valueOf(String triplet) {
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
        return new ServiceInterface(
            Transport.valueOf(matcher.group(1)),
            matcher.group(2) == null,
            Encoding.valueOf(matcher.group(3)),
            triplet
        );
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final var triplet = (ServiceInterface) other;
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
    public int compareTo(final ServiceInterface other) {
        return text.compareTo(other.text);
    }

    static {
        CACHE = new HashMap<>();
        try {
            for (final var field : ServiceInterface.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == Transport.class) {
                    final var descriptor = (ServiceInterface) field.get(null);
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
}
