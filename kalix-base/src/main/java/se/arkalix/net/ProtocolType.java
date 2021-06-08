package se.arkalix.net;

import java.util.Objects;

/**
 * Names an application-level protocol stack, such as {@link #COAP} or {@link
 * #HTTP}.
 */
@SuppressWarnings("unused")
public final class ProtocolType {
    private final String name;

    private ProtocolType(final String name) {
        this.name = name;
    }

    /**
     * Either acquires a cached protocol type matching the given name,
     * or creates a new protocol type.
     *
     * @param name  Desired protocol type name.
     * @return New or existing protocol type.
     */
    public ProtocolType getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * @return Protocol type name.
     */
    public String name() {
        return name;
    }

    /**
     * Advanced Message Queueing Protocol (AMPQ).
     *
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/amqp-core-overview-v1.0.html">OASIS Advanced Message Queuing Protocol (AMQP) Version 1.0</a>
     */
    public static final ProtocolType AMPQ = new ProtocolType("AMPQ");

    /**
     * Constrained Application Protocol (CoAP).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252">RFC 7252</a>
     * @see <a href="https://tools.ietf.org/html/rfc8323">RFC 8323</a>
     */
    public static final ProtocolType COAP = new ProtocolType("COAP");

    /**
     * Hyper-Text Transfer Protocol (HTTP).
     *
     * @see <a href="https://tools.ietf.org/html/rfc1945">RFC 1945</a>
     * @see <a href="https://tools.ietf.org/html/rfc7230">RFC 7230</a>
     * @see <a href="https://tools.ietf.org/html/rfc7231">RFC 7231</a>
     * @see <a href="https://tools.ietf.org/html/rfc7232">RFC 7232</a>
     * @see <a href="https://tools.ietf.org/html/rfc7233">RFC 7233</a>
     * @see <a href="https://tools.ietf.org/html/rfc7234">RFC 7234</a>
     * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7235</a>
     * @see <a href="https://tools.ietf.org/html/rfc7235">RFC 7540</a>
     */
    public static final ProtocolType HTTP = new ProtocolType("HTTP");

    /**
     * OASIS Message Queuing Telemetry Transport (MQTT).
     *
     * @see <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html">OASIS MQTT 5.0</a>
     * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html">OASIS MQTT 3.1.1</a>
     */
    public static final ProtocolType MQTT = new ProtocolType("MQTT");

    /**
     * Extensible Messaging and Presence Protocol (XMPP).
     *
     * @see <a href="https://tools.ietf.org/html/rfc6120">RFC 6120</a>
     * @see <a href="https://tools.ietf.org/html/rfc6121">RFC 6121</a>
     * @see <a href="https://tools.ietf.org/html/rfc7590">RFC 7590</a>
     * @see <a href="https://tools.ietf.org/html/rfc7622">RFC 7622</a>
     */
    public static final ProtocolType XMPP = new ProtocolType("XMPP");

    /**
     * Resolves {@link ProtocolType} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link ProtocolType}.
     */
    public static ProtocolType valueOf(String name) {
        name = Objects.requireNonNull(name, "Name required.").toUpperCase();
        switch (name) {
        case "AMPQ": return AMPQ;
        case "COAP": return COAP;
        case "HTTP": return HTTP;
        case "MQTT": return MQTT;
        case "XMPP": return XMPP;
        }
        return new ProtocolType(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final var protocolType = (ProtocolType) o;
        return name.equals(protocolType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
