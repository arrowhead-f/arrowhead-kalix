package se.arkalix.query;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.util.Result;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.function.ThrowingFunction;

import java.util.*;

public class ServiceQuery {
    private final String name;
    private final boolean isSecure;
    private final ThrowingFunction<ServiceQuery, Future<Set<ServiceDescription>>> resolver;

    private Collection<EncodingDescriptor> encodings;
    private Collection<TransportDescriptor> transports;
    private Map<String, String> metadata;
    private Integer version;
    private Integer versionMax;
    private Integer versionMin;

    public ServiceQuery(
        final String name,
        final boolean isSecure,
        final ThrowingFunction<ServiceQuery, Future<Set<ServiceDescription>>> resolver)
    {
        this.name = name;
        this.isSecure = isSecure;
        this.resolver = resolver;
    }

    public String name() {
        return name;
    }

    public boolean isSecure() {
        return isSecure;
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
