package se.arkalix;

import se.arkalix.description.ServiceDescription;
import se.arkalix.descriptor.EncodingDescriptor;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.TransportDescriptor;
import se.arkalix.security.access.AccessPolicy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An Arrowhead Framework service that can be provided by an {@link ArSystem}.
 */
public interface ArService {
    /**
     * @return Service name.
     */
    String name();

    /**
     * @return Service URI.
     */
    String uri();

    /**
     * @return Application-level transport protocol through which service is
     * made available to other systems.
     */
    TransportDescriptor transport();

    /**
     * @return Service access policy.
     */
    AccessPolicy accessPolicy();

    /**
     * @return Unmodifiable list of encodings the service can read and write.
     */
    List<EncodingDescriptor> encodings();

    /**
     * @return Unmodifiable map of service metadata.
     */
    Map<String, String> metadata();

    /**
     * Gets value from service metadata map associated with given {@code key},
     * if any.
     *
     * @param key Key associated with desired metadata value.
     * @return Value associated with {@code key}, if any.
     */
    default Optional<String> metadata(final String key) {
        return Optional.ofNullable(metadata().get(key));
    }

    /**
     * @return Service version.
     */
    int version();

    /**
     * Creates a description of this service.
     *
     * @param system The system owning this service.
     * @return New description.
     */
    default ServiceDescription describeAsIfProvidedBy(final ArSystem system) {
        final var isSecure = system.isSecure();
        return new ServiceDescription.Builder()
            .name(name())
            .provider(system.description())
            .uri(uri())
            .security(accessPolicy().descriptor())
            .metadata(metadata())
            .version(version())
            .interfaces(encodings().stream()
                .map(encoding -> InterfaceDescriptor.getOrCreate(transport(), isSecure, encoding))
                .collect(Collectors.toUnmodifiableList()))
            .build();
    }

}
