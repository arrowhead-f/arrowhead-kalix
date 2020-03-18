package eu.arrowhead.kalix.example;

import eu.arrowhead.kalix.dto.DataEncoding;
import eu.arrowhead.kalix.dto.Readable;
import eu.arrowhead.kalix.dto.Writable;

import java.time.Instant;
import java.util.Optional;

/**
 * A so called Data Transfer Object (DTO) interface. Such an interface may
 * only contain getters (methods that return something other than "void"
 * and accept no parameters) and is used to automatically generate classes
 * for instantiating, encoding and decoding Java objects that satisfies the
 * interface.
 * <p>
 * This particular DTO interface causes the "PingData" and "PingBuilder"
 * classes to be generated with support for reading and writing "PingData"
 * objects from/to JSON.
 */
@Readable(DataEncoding.JSON)
@Writable(DataEncoding.JSON)
interface Ping {
    /**
     * Any string.
     */
    String ping();

    /**
     * An optional identifier.
     */
    Optional<String> id();

    /**
     * Optional fields that carry anything else than arrays, lists or maps
     * must use the {@link Optional} type to indicate that they are indeed
     * optional.
     * <p>
     * DTO interfaces may include other DTO interfaces, as long as also
     * they have the same @Readable and @Writable annotations with the same
     * specified encodings.
     * <p>
     * The complete list of built-in types that are supported in DTO
     * interfaces can be read in the
     * {@code eu.arrowhead.kalix.dto.DtoPropertyFactory} class in the
     * "kalix-processors" module.
     */
    Optional<Instant> timestamp();

    /**
     * Any number of default methods may be specified without having any impact
     * on the concrete fields that will be part of the generated DTO classes.
     */
    default String asString() {
        return "Ping[ping=" + ping() + ",id=" + id().orElse("null") +
            ",timestamp=" + timestamp().map(Instant::toString).orElse("null")
            + "]";
    }
}
