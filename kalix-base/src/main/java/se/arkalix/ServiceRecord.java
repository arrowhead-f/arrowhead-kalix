package se.arkalix;

import se.arkalix._internal.DefaultServiceRecord;
import se.arkalix.security.access.AccessType;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Describes an Arrowhead service that can, potentially, be communicated with.
 */
public interface ServiceRecord extends Comparable<ServiceRecord> {
    /**
     * Gets name, or <i>service definition</i>, of this service
     *
     * @return Service name.
     */
    String name();

    /**
     * Gets description of service provider.
     *
     * @return Description of system providing this service.
     */
    SystemRecord provider();

    /**
     * Qualifier that distinguishes this service from other such provided by
     * the same system. How it is used depends on the network protocol
     * employed by the service.
     * <p>
     * For example, if this service uses HTTP, then the qualifier is a URL base
     * path, such as {@code "/base/path"}.
     *
     * @return Service qualifier.
     */
    String uri();

    /**
     * Gets time at which this service description was received or created.
     *
     * @return Record creation time.
     */
    Instant receivedAt();

    /**
     * Gets time at which this service description should be renewed or
     * refreshed.
     *
     * @return Record expiration time.
     */
    Instant expiresAt();

    /**
     * Gets security schema used to authenticate and authorize service users.
     *
     * @return Service security schema descriptor.
     */
    AccessType security();

    /**
     * Gets metadata associated with this service. Their significance and use
     * depend on the service.
     *
     * @return Service metadata.
     */
    Map<String, String> metadata();

    /**
     * Gets service version.
     *
     * @return Service version.
     */
    int version();

    /**
     * Gets interface triplets supported by the described service. The returned
     * list is unmodifiable.
     *
     * @return Service interface triplets.
     */
    default Set<ServiceInterface> interfaces() {
        return interfaceTokens().keySet();
    }

    /**
     * Gets interface triplets supported by the described service. Each triplet
     * is either associated with an authorization token or an empty string. The
     * returned map is unmodifiable.
     *
     * @return Service interface triplets mapped to authorization tokens.
     */
    Map<ServiceInterface, String> interfaceTokens();

    /**
     * Builder useful for creating {@link ServiceRecord} instances.
     */
    @SuppressWarnings("unused")
    class Builder {
        private final DefaultServiceRecord.Builder inner = new DefaultServiceRecord.Builder();

        /**
         * Service name. <b>Must be specified.</b>
         *
         * @param name Service name.
         * @return This builder.
         */
        public Builder name(final String name) {
            inner.name(name);
            return this;
        }

        /**
         * Description of system providing this service. <b>Must be
         * specified.</b>
         *
         * @param provider Providing system description.
         * @return This builder.
         */
        public Builder provider(final SystemRecord provider) {
            inner.provider(provider);
            return this;
        }

        /**
         * Sets service URI. <b>Must be specified.</b>
         *
         * @return This builder.
         * @see ServiceRecord#uri()
         */
        public Builder uri(final String uri) {
            inner.uri(uri);
            return this;
        }

        /**
         * Sets time at which this service description is to be considered to
         * have been received from its source.
         *
         * @param receivedAt Instant at which this service description was
         *                   learned about.
         * @return This builder.
         */
        public Builder receivedAt(final Instant receivedAt) {
            inner.receivedAt(receivedAt);
            return this;
        }

        /**
         * Sets time at which this service description should be renewed or
         * refreshed.
         *
         * @param expiresAt Instant soon after which this service description
         *                  should be renewed or refreshed.
         * @return This builder.
         */
        public Builder expiresAt(final Instant expiresAt) {
            inner.expiresAt(expiresAt);
            return this;
        }

        /**
         * Sets security descriptor. <b>Must be specified.</b>
         *
         * @param security Security descriptor.
         * @return This builder.
         */
        public Builder security(final AccessType security) {
            inner.security(security);
            return this;
        }

        /**
         * Sets service metadata.
         *
         * @param metadata Service metadata.
         * @return This builder.
         */
        public Builder metadata(final Map<String, String> metadata) {
            inner.metadata(metadata);
            return this;
        }

        /**
         * Sets service version. Defaults to 0.
         *
         * @param version Service version.
         * @return This builder.
         */
        public Builder version(final int version) {
            inner.version(version);
            return this;
        }

        /**
         * Sets interface triplets supported by service. <b>At least one must
         * be specified, either using this method or {@link
         * #interfaceTokens(Map)}.</b>
         *
         * @param interfaces Interface triplets.
         * @return This builder.
         */
        public Builder interfaces(final ServiceInterface... interfaces) {
            return interfaces(Arrays.asList(interfaces));
        }

        /**
         * Sets interface triplets supported by service. <b>At least one must
         * be specified, either using this method or {@link
         * #interfaceTokens(Map)}.</b>
         *
         * @param interfaces Interface triplets.
         * @return This builder.
         */
        public Builder interfaces(final Collection<ServiceInterface> interfaces) {
            return interfaceTokens(interfaces.stream()
                .collect(Collectors.toMap(descriptor -> descriptor, ignored -> "")));
        }

        /**
         * Sets interface triplets supported by service, where each triplet is
         * assigned an authorization token. <b>At least one must be specified,
         * either using this method or {@link #interfaces(Collection)}.</b>
         * <p>
         * If any interface is not associated with a token, but it still is
         * relevant to include it in the map, use an empty string as token.
         *
         * @param interfaceTokens Interface triplets mapped to authorization
         *                        tokens.
         * @return This builder.
         */
        public Builder interfaceTokens(final Map<ServiceInterface, String> interfaceTokens) {
            inner.interfaceTokens(interfaceTokens);
            return this;
        }

        /**
         * @return New {@link ServiceRecord}.
         */
        public ServiceRecord build() {
            return inner.build();
        }
    }
}
