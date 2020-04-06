package se.arkalix.query;

import se.arkalix.ArConsumer;
import se.arkalix.ArConsumerFactory;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.function.ThrowingFunction;

import java.util.*;

public class ServiceQuery {
    private final ArSystem consumer;
    private final String name;
    private final ThrowingFunction<ServiceQuery, Future<Set<ServiceDescription>>> resolver;

    private Collection<EncodingDescriptor> encodings;
    private Collection<TransportDescriptor> transports;
    private Map<String, String> metadata;
    private Integer version;
    private Integer versionMax;
    private Integer versionMin;

    public ServiceQuery(
        final ArSystem consumer,
        final String serviceName,
        final ThrowingFunction<ServiceQuery, Future<Set<ServiceDescription>>> resolver)
    {
        this.consumer = Objects.requireNonNull(consumer, "Expected consumer");
        this.name = Objects.requireNonNull(serviceName, "Expected serviceName");
        this.resolver = Objects.requireNonNull(resolver, "Expected resolver");
    }

    public String name() {
        return name;
    }

    public boolean isSecure() {
        return consumer.isSecure();
    }

    public Collection<TransportDescriptor> transports() {
        return transports != null ? transports : Collections.emptyList();
    }

    public ServiceQuery transports(final Collection<TransportDescriptor> transports) {
        this.transports = transports;
        return this;
    }

    public ServiceQuery transports(final TransportDescriptor... transports) {
        this.transports = Arrays.asList(transports);
        return this;
    }

    public Collection<EncodingDescriptor> encodings() {
        return encodings != null ? encodings : Collections.emptyList();
    }

    public ServiceQuery encodings(final Collection<EncodingDescriptor> encodings) {
        this.encodings = encodings;
        return this;
    }

    public ServiceQuery encodings(final EncodingDescriptor... encodings) {
        this.encodings = Arrays.asList(encodings);
        return this;
    }

    public Map<String, String> metadata() {
        return metadata != null ? metadata : Collections.emptyMap();
    }

    public ServiceQuery metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Optional<Integer> version() {
        return Optional.ofNullable(version);
    }

    public ServiceQuery version(final Integer version) {
        this.version = version;
        return this;
    }

    public Optional<Integer> versionMax() {
        return Optional.ofNullable(versionMax);
    }

    public ServiceQuery versionMax(final Integer versionMax) {
        this.versionMax = versionMax;
        return this;
    }

    public Optional<Integer> versionMin() {
        return Optional.ofNullable(versionMin);
    }

    public ServiceQuery versionMin(final Integer versionMin) {
        this.versionMin = versionMin;
        return this;
    }

    public <C extends ArConsumer> Future<C> using(final ArConsumerFactory<C> factory) {
        Collection<TransportDescriptor> currentTransports = Collections.emptyList();
        if (transports == null) {
            transports = factory.supportedTransports();
        }
        else {
            currentTransports = transports;
            transports.retainAll(factory.supportedTransports());
        }
        if (transports.isEmpty()) {
            return Future.failure(new IllegalStateException("The provided " +
                "consumer factory \"" + factory + "\" only supports the " +
                "following application-level transport protocols: " +
                factory.supportedTransports() + ", while this query was " +
                "already configured to require that any out of " +
                currentTransports + " be supported; no factory-" +
                "supported transports are present in the existing " +
                "collection; cannot create consumer"));
        }

        Collection<EncodingDescriptor> currentEncodings = Collections.emptyList();
        if (encodings == null) {
            encodings = factory.supportedEncodings();
        }
        else {
            encodings.retainAll(factory.supportedEncodings());
        }
        if (encodings.isEmpty()) {
            return Future.failure(new IllegalStateException("The provided " +
                "consumer factory \"" + factory + "\" only supports the " +
                "following encodings: " + factory.supportedEncodings() + ", " +
                "while this query was already configured to require that " +
                "any out of " + currentEncodings + " be supported; no " +
                "factory-supported encodings are present in the existing " +
                "collection; cannot create consumer"));
        }

        return resolveOne().map(service -> factory.create(consumer, service, encodings));
    }

    public Future<Set<ServiceDescription>> resolveAll() {
        try {
            return resolver.apply(this);
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    public Future<ServiceDescription> resolveOne() {
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
}
