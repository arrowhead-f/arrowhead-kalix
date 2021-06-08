package se.arkalix;

import se.arkalix.codec.CodecType;
import se.arkalix.net.ProtocolType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A network interface type triplet.
 * <p>
 * Each provided Arrowhead {@link se.arkalix.ArService service} exposes its
 * functionality via at least one <i>interface</i>. An interface consists of a
 * {@link ProtocolType protocol}, a requirement to either use
 * or not to use <a href="https://tools.ietf.org/html/rfc7925">TLS/DTLS</a>, as
 * well as a {@link CodecType payload codec}. When it is advertised what
 * interfaces a certain service supports, each of those interfaces will be
 * represented by an instance of this descriptor.
 */
@SuppressWarnings("unused")
public final class ServiceInterface implements Comparable<ServiceInterface> {
    private static final Pattern TRIPLET_PATTERN = Pattern.compile("^([0-9A-Z_]+)-(IN)?SECURE-([0-9A-Z_]+)$");

    private static final HashMap<ProtocolType, List<ServiceInterface>> CACHE;

    private final ProtocolType protocolType;
    private final boolean isSecure;
    private final CodecType codecType;
    private final String name;

    private ServiceInterface(
        final ProtocolType protocolType,
        final boolean isSecure,
        final CodecType codecType,
        final String name
    ) {
        this.protocolType = protocolType;
        this.isSecure = isSecure;
        this.codecType = codecType;
        this.name = name;
    }

    /**
     * Either acquires a cached interface descriptor matching the given
     * arguments, or uses them to create a new descriptor.
     *
     * @param protocolType Protocol type.
     * @param isSecure     Whether transport security is to be used.
     * @param codecType    Codec type.
     * @return New or existing interface descriptor.
     */
    public static ServiceInterface getOrCreate(
        final ProtocolType protocolType,
        final boolean isSecure,
        final CodecType codecType
    ) {
        if (isSecure) {
            final var candidates = CACHE.get(protocolType);
            if (candidates != null) {
                for (final var candidate : candidates) {
                    if (candidate.codecType == codecType) {
                        return candidate;
                    }
                }
            }
        }
        return new ServiceInterface(protocolType, isSecure, codecType,
            protocolType + (isSecure ? "-SECURE-" : "-INSECURE-") + codecType);
    }

    /**
     * Gets interface protocol type.
     *
     * @return Protocol type.
     */
    public ProtocolType protocolType() {
        return protocolType;
    }

    /**
     * Gets interface security mode.
     *
     * @return Whether or not transport security is to be used.
     */
    public boolean isSecure() {
        return isSecure;
    }

    /**
     * Gets interface codec type.
     *
     * @return Message codec descriptor.
     */
    public CodecType codecType() {
        return codecType;
    }

    /**
     * Gets textual description of this interface.
     *
     * @return Textual description of interface, such as "HTTP-SECURE-JSON".
     */
    public String name() {
        return name;
    }

    /**
     * AMPQ over TLS with CBOR payloads.
     *
     * @see ProtocolType#AMPQ
     * @see CodecType#CBOR
     */
    public static final ServiceInterface AMPQ_SECURE_CBOR = new ServiceInterface(
        ProtocolType.AMPQ, true, CodecType.CBOR, "AMPQ-SECURE-CBOR");

    /**
     * AMPQ over TLS with JSON payloads.
     *
     * @see ProtocolType#AMPQ
     * @see CodecType#JSON
     */
    public static final ServiceInterface AMPQ_SECURE_JSON = new ServiceInterface(
        ProtocolType.AMPQ, true, CodecType.JSON, "AMPQ-SECURE-JSON");

    /**
     * AMPQ over TLS with XML payloads.
     *
     * @see ProtocolType#AMPQ
     * @see CodecType#XML
     */
    public static final ServiceInterface AMPQ_SECURE_XML = new ServiceInterface(
        ProtocolType.AMPQ, true, CodecType.XML, "AMPQ-SECURE-XML");

    /**
     * AMPQ over TLS with XSI payloads.
     *
     * @see ProtocolType#AMPQ
     * @see CodecType#EXI
     */
    public static final ServiceInterface AMPQ_SECURE_XSI = new ServiceInterface(
        ProtocolType.AMPQ, true, CodecType.EXI, "AMPQ-SECURE-XSI");

    /**
     * CoAP over TLS with CBOR payloads.
     *
     * @see ProtocolType#COAP
     * @see CodecType#CBOR
     */
    public static final ServiceInterface COAP_SECURE_CBOR = new ServiceInterface(
        ProtocolType.COAP, true, CodecType.CBOR, "COAP-SECURE-CBOR");

    /**
     * CoAP over TLS with JSON payloads.
     *
     * @see ProtocolType#COAP
     * @see CodecType#JSON
     */
    public static final ServiceInterface COAP_SECURE_JSON = new ServiceInterface(
        ProtocolType.COAP, true, CodecType.JSON, "COAP-SECURE-JSON");

    /**
     * CoAP over TLS with XML payloads.
     *
     * @see ProtocolType#COAP
     * @see CodecType#XML
     */
    public static final ServiceInterface COAP_SECURE_XML = new ServiceInterface(
        ProtocolType.COAP, true, CodecType.XML, "COAP-SECURE-XML");

    /**
     * CoAP over TLS with XSI payloads.
     *
     * @see ProtocolType#COAP
     * @see CodecType#EXI
     */
    public static final ServiceInterface COAP_SECURE_XSI = new ServiceInterface(
        ProtocolType.COAP, true, CodecType.EXI, "COAP-SECURE-XSI");

    /**
     * HTTPS with CBOR payloads.
     *
     * @see ProtocolType#HTTP
     * @see CodecType#CBOR
     */
    public static final ServiceInterface HTTP_SECURE_CBOR = new ServiceInterface(
        ProtocolType.HTTP, true, CodecType.CBOR, "HTTP-SECURE-CBOR");

    /**
     * HTTPS with JSON payloads.
     *
     * @see ProtocolType#HTTP
     * @see CodecType#JSON
     */
    public static final ServiceInterface HTTP_SECURE_JSON = new ServiceInterface(
        ProtocolType.HTTP, true, CodecType.JSON, "HTTP-SECURE-JSON");

    /**
     * HTTPS with XML payloads.
     *
     * @see ProtocolType#HTTP
     * @see CodecType#XML
     */
    public static final ServiceInterface HTTP_SECURE_XML = new ServiceInterface(
        ProtocolType.HTTP, true, CodecType.XML, "HTTP-SECURE-XML");

    /**
     * HTTPS with XSI payloads.
     *
     * @see ProtocolType#HTTP
     * @see CodecType#EXI
     */
    public static final ServiceInterface HTTP_SECURE_XSI = new ServiceInterface(
        ProtocolType.HTTP, true, CodecType.EXI, "HTTP-SECURE-XSI");

    /**
     * MQTT over TLS with CBOR payloads.
     *
     * @see ProtocolType#MQTT
     * @see CodecType#CBOR
     */
    public static final ServiceInterface MQTT_SECURE_CBOR = new ServiceInterface(
        ProtocolType.MQTT, true, CodecType.CBOR, "MQTT-SECURE-CBOR");
    /**
     * MQTT over TLS with JSON payloads.
     *
     * @see ProtocolType#MQTT
     * @see CodecType#JSON
     */
    public static final ServiceInterface MQTT_SECURE_JSON = new ServiceInterface(
        ProtocolType.MQTT, true, CodecType.JSON, "MQTT-SECURE-JSON");

    /**
     * MQTT over TLS with XML payloads.
     *
     * @see ProtocolType#MQTT
     * @see CodecType#XML
     */
    public static final ServiceInterface MQTT_SECURE_XML = new ServiceInterface(
        ProtocolType.MQTT, true, CodecType.XML, "MQTT-SECURE-XML");

    /**
     * MQTT over TLS with XSI payloads.
     *
     * @see ProtocolType#MQTT
     * @see CodecType#EXI
     */
    public static final ServiceInterface MQTT_SECURE_XSI = new ServiceInterface(
        ProtocolType.MQTT, true, CodecType.EXI, "MQTT-SECURE-XSI");

    /**
     * XMPP over TLS with CBOR payloads.
     *
     * @see ProtocolType#XMPP
     * @see CodecType#CBOR
     */
    public static final ServiceInterface XMPP_SECURE_CBOR = new ServiceInterface(
        ProtocolType.XMPP, true, CodecType.CBOR, "XMPP-SECURE-CBOR");

    /**
     * XMPP over TLS with JSON payloads.
     *
     * @see ProtocolType#XMPP
     * @see CodecType#JSON
     */
    public static final ServiceInterface XMPP_SECURE_JSON = new ServiceInterface(
        ProtocolType.XMPP, true, CodecType.JSON, "XMPP-SECURE-JSON");

    /**
     * XMPP over TLS with XML payloads.
     *
     * @see ProtocolType#XMPP
     * @see CodecType#XML
     */
    public static final ServiceInterface XMPP_SECURE_XML = new ServiceInterface(
        ProtocolType.XMPP, true, CodecType.XML, "XMPP-SECURE-XML");

    /**
     * XMPP over TLS with XSI payloads.
     *
     * @see ProtocolType#XMPP
     * @see CodecType#EXI
     */
    public static final ServiceInterface XMPP_SECURE_XSI = new ServiceInterface(
        ProtocolType.XMPP, true, CodecType.EXI, "XMPP-SECURE-XSI");

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
            ProtocolType.valueOf(matcher.group(1)),
            matcher.group(2) == null,
            CodecType.valueOf(matcher.group(3)),
            triplet
        );
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final var triplet = (ServiceInterface) other;
        return protocolType.equals(triplet.protocolType) &&
            isSecure == triplet.isSecure &&
            codecType.equals(triplet.codecType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocolType, isSecure, codecType);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final ServiceInterface other) {
        return name.compareTo(other.name);
    }

    static {
        CACHE = new HashMap<>();
        try {
            for (final var field : ServiceInterface.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == ProtocolType.class) {
                    final var descriptor = (ServiceInterface) field.get(null);
                    CACHE.compute(descriptor.protocolType, (key, value) -> {
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
