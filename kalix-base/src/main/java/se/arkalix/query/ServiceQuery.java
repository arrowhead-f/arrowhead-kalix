package se.arkalix.query;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.net.Encoding;
import se.arkalix.net.Transport;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.function.ThrowingFunction;

import java.util.*;
import java.util.stream.Stream;

/**
 * A description of an Arrowhead service some {@link ArSystem} wishes to
 * consume.
 * <p>
 * The class is meant to be used as follows:
 * <ol>
 *     <li>An instance is created using {@link ArSystem#consume()}.</li>
 *     <li>The methods of this class are used to specify as much information as
 *         is required for relevant services to be resolved by the resolver
 *         function provided at {@link
 *         ServiceQuery#ServiceQuery(ArSystem, ThrowingFunction) instance
 *         creation}. How much information is required will depend on the used
 *         resolution strategy, which might vary from just the name of the
 *         service to all possible details about it.</li>
 *     <li>One out of the methods {@link ServiceQuery#allUsing(ArConsumerFactory)
 *         allUsing()} {@link ServiceQuery#oneUsing(ArConsumerFactory)
 *         oneUsing()}, {@link ServiceQuery#resolveAll() resolveAll()} and
 *         {@link ServiceQuery#resolveOne() resolveOne()} are used to complete
 *         the query and start service resolution. If any of the {@link
 *         ServiceQuery#oneUsing(ArConsumerFactory) oneUsing()} or {@link
 *         ServiceQuery#allUsing(ArConsumerFactory) allUsing()} method are
 *         used, the provided factory automatically provides additional details
 *         to the query before it is executed. Make sure to read the
 *         documentation for the {@link ArConsumerFactory} you want to use with
 *         that method in order to be certain about what details will be
 *         automatically provided. {@link ArConsumerFactory} classes
 *         representing specific Arrowhead services typically require no query
 *         information to be explicitly specified at all.
 *     </li>
 * </ol>
 */
@SuppressWarnings("unused")
public class ServiceQuery {
    private final ArSystem consumer;
    private final ThrowingFunction<ServiceQuery, Future<Set<ServiceRecord>>> resolver;

    private String name;
    private Collection<Encoding> encodings;
    private Collection<Transport> transports;
    private Map<String, String> metadata;
    private Integer version;
    private Integer versionMax;
    private Integer versionMin;

    /**
     * Creates new service query.
     *
     * @param consumer System to consume the service resolved by the query.
     * @param resolver Function used to resolve the query when it
     */
    public ServiceQuery(
        final ArSystem consumer,
        final ThrowingFunction<ServiceQuery, Future<Set<ServiceRecord>>> resolver
    ) {
        this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
        this.resolver = Objects.requireNonNull(resolver, "Expected resolver");
    }

    /**
     * @return Name of service to be consumed.
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * @param name Name of service to be consumed.
     * @return This query.
     */
    public ServiceQuery name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @return {@code true} only if the system consuming the result of this
     * query is running in secure mode.
     */
    public boolean isSecure() {
        return consumer.isSecure();
    }

    /**
     * @return Application-level transport protocols that may be employed by
     * the consumed service.
     */
    public Collection<Transport> transports() {
        return transports != null ? transports : Collections.emptyList();
    }

    /**
     * @param transports Application-level transport protocols that may be
     *                   employed by the consumed service.
     * @return This query.
     */
    public ServiceQuery transports(final Collection<Transport> transports) {
        this.transports = transports;
        return this;
    }

    /**
     * @param transports Application-level transport protocols that may be
     *                   employed by the consumed service.
     * @return This query.
     */
    public ServiceQuery transports(final Transport... transports) {
        this.transports = Arrays.asList(transports);
        return this;
    }

    /**
     * @return Message payload encodings that may be employed by the consumed
     * service.
     */
    public Collection<Encoding> encodings() {
        return encodings != null ? encodings : Collections.emptyList();
    }

    /**
     * @param encodings Message payload encodings that may be employed by the
     *                  consumed service.
     * @return This query.
     */
    public ServiceQuery encodings(final Collection<Encoding> encodings) {
        this.encodings = encodings;
        return this;
    }

    /**
     * @param encodings Message payload encodings that may be employed by the
     *                  consumed service.
     * @return This query.
     */
    public ServiceQuery encodings(final Encoding... encodings) {
        this.encodings = Arrays.asList(encodings);
        return this;
    }

    /**
     * @return Metadata pairs that must be matched by the consumed service.
     */
    public Map<String, String> metadata() {
        return metadata != null ? metadata : Collections.emptyMap();
    }

    /**
     * @param metadata Metadata pairs that must be matched by the consumed
     *                 service.
     * @return This query.
     */
    public ServiceQuery metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return Version, if any, that must be matched by the consumed service.
     */
    public Optional<Integer> version() {
        return Optional.ofNullable(version);
    }

    /**
     * @param version Version that must be matched by the consumed service.
     * @return This query.
     */
    public ServiceQuery version(final Integer version) {
        this.version = version;
        return this;
    }

    /**
     * @return Maximum version, if any, of the consumed version.
     */
    public Optional<Integer> versionMax() {
        return Optional.ofNullable(versionMax);
    }

    /**
     * @param versionMax Maximum version of the consumed version.
     * @return This query.
     */
    public ServiceQuery versionMax(final Integer versionMax) {
        this.versionMax = versionMax;
        return this;
    }

    /**
     * @return Minimum version, if any, of the consumed version.
     */
    public Optional<Integer> versionMin() {
        return Optional.ofNullable(versionMin);
    }

    /**
     * @param versionMin Minimum version of the consumed version.
     * @return This query.
     */
    public ServiceQuery versionMin(final Integer versionMin) {
        this.versionMin = versionMin;
        return this;
    }

    /**
     * Uses provided {@code factory} to (1) add additional details to this
     * query, (2) resolve the query, and then (3) use the resolution result to
     * construct a stream of {@link ArConsumer} instances, which may be used to
     * consume those service by exchanging messages with them.
     *
     * @param factory Class useful for creating {@link ArConsumer} instances.
     * @param <C>     Type of {@link ArConsumer}.
     * @return {@link Future} completed with {@link ArConsumer}, if service
     * resolution succeeded.
     * @see se.arkalix.net.http.consumer
     */
    public <C extends ArConsumer> Future<Stream<C>> allUsing(final ArConsumerFactory<C> factory) {
        final var throwable = updateAndValidateUsing(factory);
        if (throwable != null) {
            return Future.failure(throwable);
        }
        return resolveAll()
            .map(services -> services.stream()
                .map(service -> factory.create(consumer, service, encodings)));
    }

    /**
     * Uses provided {@code factory} to (1) add additional details to this
     * query, (2) resolve the query, and then (3) use the resolution result to
     * construct one {@link ArConsumer} instance, which may be used to consume
     * the service by exchanging messages with it.
     *
     * @param factory Class useful for creating {@link ArConsumer} instances.
     * @param <C>     Type of {@link ArConsumer}.
     * @return {@link Future} completed with {@link ArConsumer}, if service
     * resolution succeeded. If service resolution did not fail but yielded no
     * matching services, the {@link Future} is failed with a {@link
     * ServiceNotFoundException}.
     * @see se.arkalix.net.http.consumer
     */
    public <C extends ArConsumer> Future<C> oneUsing(final ArConsumerFactory<C> factory) {
        final var throwable = updateAndValidateUsing(factory);
        if (throwable != null) {
            return Future.failure(throwable);
        }
        return resolveOne()
            .map(service -> factory.create(consumer, service, encodings));
    }

    private Throwable updateAndValidateUsing(final ArConsumerFactory<?> factory) {
        // Set and check service name.
        final var optionalName = factory.serviceName();
        if (name == null) {
            if (optionalName.isEmpty()) {
                return new IllegalStateException("No service " +
                    "name was specified, neither explicitly or by the " +
                    "provided consumer factory \"" + factory + "\"; cannot " +
                    "resolve service");
            }
            name = optionalName.get();
        }
        else {
            if (optionalName.isPresent()) {
                final var name0 = optionalName.get();
                if (!name.equals(name0)) {
                    return new IllegalStateException("The " +
                        "service name \"" + name + "\" was explicitly " +
                        "specified, but the provided consumer factory \"" +
                        factory + "\" requires that the name \"" + name0
                        + "\" be used; cannot resolve service");
                }
            }
        }

        // Set and check service transports.
        Collection<Transport> currentTransports = Collections.emptyList();
        if (transports == null) {
            transports = factory.serviceTransports();
        }
        else {
            currentTransports = transports;
            transports.retainAll(factory.serviceTransports());
        }
        if (transports.isEmpty()) {
            return new IllegalStateException("The provided " +
                "consumer factory \"" + factory + "\" only supports the " +
                "following application-level transport protocols: " +
                factory.serviceTransports() + ", while this query was " +
                "already configured to require that any out of " +
                currentTransports + " be supported; no factory-" +
                "supported transports are present in the existing " +
                "collection; cannot resolve service");
        }

        // Set and check service encodings.
        Collection<Encoding> currentEncodings = Collections.emptyList();
        if (encodings == null) {
            encodings = factory.serviceEncodings();
        }
        else {
            encodings.retainAll(factory.serviceEncodings());
        }
        if (encodings.isEmpty()) {
            return new IllegalStateException("The provided " +
                "consumer factory \"" + factory + "\" only supports the " +
                "following encodings: " + factory.serviceEncodings() + ", " +
                "while this query was already configured to require that " +
                "any out of " + currentEncodings + " be supported; no " +
                "factory-supported encodings are present in the existing " +
                "collection; cannot resolve service");
        }

        // Set and check service metadata.
        final var metadata0 = factory.serviceMetadata();
        if (metadata == null) {
            metadata = metadata0;
        }
        else {
            for (final var entry : metadata0.entrySet()) {
                final var key0 = entry.getKey();
                final var value0 = entry.getValue();
                final var value2 = metadata.compute(key0, (key1, value1) -> {
                    if (value1 == null || value1.equals(value0)) {
                        return value0;
                    }
                    return null;
                });
                if (value2 == null) {
                    return new IllegalStateException("The " +
                        "provided consumer factory \"" + factory + "\" " +
                        "requires that the metadata field \"" + key0 + "\" " +
                        "be set to \"" + value0 + "\", but the field has " +
                        "been explicitly set to \"" + metadata.get(key0) +
                        "\"; cannot create consumer");
                }
            }
        }

        // Set and check service version.
        final var version0 = factory.serviceVersion().orElse(null);
        if (!Objects.equals(version, version0)) {
            return new IllegalStateException("The provided " +
                "consumer factory \"" + factory + "\" only supports service " +
                "version " + version0 + ", while version " + version +
                " was explicitly specified; cannot create consumer");
        }
        version = version0;

        // Set and check service version min/max range.
        final var versionMin0 = factory.serviceVersionMin().orElse(null);
        final int versionMin1;
        if (versionMin == null && versionMin0 != null) {
            versionMin1 = versionMin0;
        }
        else if (versionMin0 != null) {
            versionMin1 = Math.max(versionMin, versionMin0);
        }
        else {
            versionMin1 = 0;
        }
        final var versionMax0 = factory.serviceVersionMax().orElse(null);
        final int versionMax1;
        if (versionMax == null && versionMax0 != null) {
            versionMax1 = versionMax0;
        }
        else if (versionMax0 != null) {
            versionMax1 = Math.min(versionMax, versionMax0);
        }
        else {
            versionMax1 = Integer.MAX_VALUE;
        }
        if (versionMin1 > versionMax1) {
            return new IllegalStateException("The provided " +
                "consumer factory \"" + factory + "\" only supports service " +
                "versions in the range " + versionMin0 + ".." + versionMax0 +
                ", while the non-overlapping range " +
                (versionMin != null ? versionMin : "") + ".." +
                (versionMax != null ? versionMax : "") + " was " +
                "explicitly specified; cannot create consumer");
        }
        if (versionMax1 != Integer.MAX_VALUE) {
            versionMax = versionMax1;
        }
        if (versionMin1 != 0) {
            versionMin = versionMin1;
        }
        if (version != null && (version < versionMin1 || version > versionMax1)) {
            return new IllegalStateException("Taken " +
                "together, the service versions supported by both the " +
                "provided consumer factory \"" + factory + "\" and the " +
                "explicitly provided versions are in the range " +
                versionMin1 + ".." + versionMax1 + ", but the " +
                (version0 == null ? "explicitly provided version " :
                    "version required by the consumer factory ") + version
                + " is not in that range; cannot create consumer");
        }

        return null;
    }

    /**
     * Uses resolver provided at {@link
     * ServiceQuery#ServiceQuery(ArSystem, ThrowingFunction) instance creation}
     * to lookup all services matching this query.
     *
     * @return {@link Future} completed, if successful, with a set of service
     * descriptions matching this query.
     */
    public Future<Set<ServiceRecord>> resolveAll() {
        try {
            return resolver.apply(this);
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    /**
     * Uses resolver provided at {@link
     * ServiceQuery#ServiceQuery(ArSystem, ThrowingFunction) instance creation}
     * to lookup exactly one service matching this query.
     *
     * @return {@link Future} completed, if successful, with exactly one
     * service description matching this query. If no such service could be
     * resolved, the {@link Future} is failed with a {@link
     * ServiceNotFoundException}.
     */
    public Future<ServiceRecord> resolveOne() {
        return resolveAll().mapResult(result -> {
            if (result.isFailure()) {
                return Result.failure(result.fault());
            }
            return result.value()
                .stream()
                .findFirst()
                .map(Result::success)
                .orElseGet(() -> Result.failure(new ServiceNotFoundException(this)));
        });
    }

    /**
     * Tests whether this query matches the given {@code service} description.
     *
     * @param service Service description to test.
     * @return {@code true} only if the given {@code service} satisfies the
     * requirements of this query.
     */
    public boolean matches(final ServiceRecord service) {
        if (name != null && !name.equals(service.name())) {
            return false;
        }

        final var serviceInterfaces = service.interfaces();

        final var queryTransports = transports();
        if (!queryTransports.isEmpty() && serviceInterfaces.stream()
            .noneMatch(triplet -> queryTransports.contains(triplet.transport())))
        {
            return false;
        }

        final var queryEncodings = encodings();
        if (!queryEncodings.isEmpty() && serviceInterfaces.stream()
            .noneMatch(triplet -> queryEncodings.contains(triplet.encoding())))
        {
            return false;
        }

        if (metadata != null && !metadata.isEmpty() && !service.metadata()
            .entrySet().containsAll(metadata.entrySet()))
        {
            return false;
        }

        final var serviceVersion = service.version();

        if (version != null && serviceVersion != version) {
            return false;
        }

        if (versionMax != null && serviceVersion > versionMax) {
            return false;
        }

        if (versionMin != null && serviceVersion < versionMin) {
            return false;
        }

        return service.security().isSecure() == isSecure();
    }

    @Override
    public String toString() {
        return "ServiceQuery{" +
            "consumer=" + consumer +
            ", resolver=" + resolver +
            ", name='" + name + '\'' +
            ", encodings=" + encodings +
            ", transports=" + transports +
            ", metadata=" + metadata +
            ", version=" + version +
            ", versionMax=" + versionMax +
            ", versionMin=" + versionMin +
            '}';
    }
}
