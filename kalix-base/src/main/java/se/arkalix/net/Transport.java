package se.arkalix.net;

import java.util.Objects;

/**
 * Names an application-level transport protocol stack, such as {@link #COAP}
 * or {@link #HTTP}.
 */
@SuppressWarnings("unused")
public final class Transport {
    private final String name;

    private Transport(final String name) {
        this.name = name;
    }

    /**
     * Either acquires a cached transport descriptor matching the given name,
     * or creates a new descriptor.
     *
     * @param name  Desired transport descriptor name.
     * @return New or existing transport descriptor.
     */
    public Transport getOrCreate(final String name) {
        return valueOf(name);
    }

    /**
     * @return Transport identifier.
     */
    public String name() {
        return name;
    }

    /**
     * Advanced Message Queueing Protocol (AMPQ).
     *
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/amqp-core-overview-v1.0.html">OASIS Advanced Message Queuing Protocol (AMQP) Version 1.0</a>
     */
    public static final Transport AMPQ = new Transport("AMPQ");

    /**
     * Constrained Application Protocol (CoAP).
     *
     * @see <a href="https://tools.ietf.org/html/rfc7252">RFC 7252</a>
     * @see <a href="https://tools.ietf.org/html/rfc8323">RFC 8323</a>
     */
    public static final Transport COAP = new Transport("COAP");

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
    public static final Transport HTTP = new Transport("HTTP");

    /**
     * OASIS Message Queuing Telemetry Transport (MQTT).
     *
     * @see <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/mqtt-v5.0.html">OASIS MQTT 5.0</a>
     * @see <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/mqtt-v3.1.1.html">OASIS MQTT 3.1.1</a>
     */
    public static final Transport MQTT = new Transport("MQTT");

    /**
     * Extensible Messaging and Presence Protocol (XMPP).
     *
     * @see <a href="https://tools.ietf.org/html/rfc6120">RFC 6120</a>
     * @see <a href="https://tools.ietf.org/html/rfc6121">RFC 6121</a>
     * @see <a href="https://tools.ietf.org/html/rfc7590">RFC 7590</a>
     * @see <a href="https://tools.ietf.org/html/rfc7622">RFC 7622</a>
     */
    public static final Transport XMPP = new Transport("XMPP");

    /**
     * Resolves {@link Transport} from given {@code name}.
     *
     * @param name Name to resolve. Case insensitive.
     * @return Cached or new {@link Transport}.
     */
    public static Transport valueOf(String name) {
        name = Objects.requireNonNull(name, "Name required.").toUpperCase();
        switch (name) {
        case "AMPQ": return AMPQ;
        case "COAP": return COAP;
        case "HTTP": return HTTP;
        case "MQTT": return MQTT;
        case "XMPP": return XMPP;
        }
        return new Transport(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final var transport = (Transport) o;
        return name.equals(transport.name);
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
