package se.arkalix.description;

import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.internal.net.dns.DnsNames;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Describes an Arrowhead Framework service, as provided by a local or
 * remote {@link se.arkalix.ArSystem system}.
 */
public class ServiceDescription implements Comparable<ServiceDescription> {
    private final String name;
    private final ProviderDescription provider;
    private final String uri;
    private final Instant receivedAt;
    private final Instant expiresAt;
    private final SecurityDescriptor security;
    private final Map<String, String> metadata;
    private final int version;
    private final Map<InterfaceDescriptor, String> interfaceTokens;

    private ServiceDescription(final Builder builder) {
        name = Objects.requireNonNull(builder.name, "Expected name");
        if (!DnsNames.isLabel(name)) {
            throw new IllegalArgumentException("Name \"" + name + "\" is " +
                "not a valid DNS label; such a label may only contain the " +
                "characters `0-9 A-Z a-z -`, must not start with a digit or " +
                "a dash, and must not end with a dash");
        }
        provider = Objects.requireNonNull(builder.provider, "Expected provider");
        uri = Objects.requireNonNull(builder.uri, "Expected uri");
        if (uri.isBlank()) {
            throw new IllegalArgumentException("Blank or null URI" +
                "qualifiers are not permitted");
        }
        receivedAt = Objects.requireNonNullElseGet(builder.receivedAt, Instant::now);
        expiresAt = Objects.requireNonNullElse(builder.expiresAt, Instant.MAX);
        security = Objects.requireNonNull(builder.security, "Expected security");
        metadata = builder.metadata == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(builder.metadata);
        version = builder.version;
        interfaceTokens = Collections.unmodifiableMap(
            Objects.requireNonNull(builder.interfaceTokens, "Expected interfaceTokens"));
        if (interfaceTokens.size() == 0) {
            throw new IllegalArgumentException("At least one supported interface must be specified");
        }
    }

    /**
     * @return Name, or <i>service definition</i>, of this service.
     */
    public String name() {
        return name;
    }

    /**
     * @return Description of system providing this service.
     */
    public ProviderDescription provider() {
        return provider;
    }

    /**
     * Qualifier that distinguishes this service from other such provided by
     * the same system. How it is used depends on the application-level
     * transport protocol employed by the service.
     * <p>
     * For example, if this service uses HTTP, then the qualifier is a URL base
     * path, such as {@code "/base/path"}.
     *
     * @return Service qualifier.
     */
    public String uri() {
        return uri;
    }

    /**
     * @return Time at which this service description was received or created.
     */
    public Instant receivedAt() {
        return receivedAt;
    }

    /**
     * @return Time at which this service description should be renewed or
     * refreshed.
     */
    public Instant expiresAt() {
        return expiresAt;
    }

    /**
     * @return Security schema used to authenticate and authorize service
     * users.
     */
    public SecurityDescriptor security() {
        return security;
    }

    /**
     * @return Metadata associated with this service. Their significance and
     * use depend on the service.
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * @return Service version.
     */
    public int version() {
        return version;
    }

    /**
     * @return Interface triplets supported by the described service. The
     * returned list is unmodifiable.
     */
    public Set<InterfaceDescriptor> interfaces() {
        return interfaceTokens.keySet();
    }

    /**
     * @return Interface triplets supported by the described service. Each
     * triplet is either associated with an authorization token or an empty
     * string. The returned map is unmodifiable.
     */
    public Map<InterfaceDescriptor, String> interfaceTokens() {
        return interfaceTokens;
    }

    @Override
    public int compareTo(final ServiceDescription other) {
        int d;
        d = name().compareTo(other.name());
        if (d != 0) {
            return d;
        }
        d = uri().compareTo(other.uri());
        if (d != 0) {
            return d;
        }
        final var aInterfaces = interfaces();
        final var bInterfaces = other.interfaces();
        if (aInterfaces.size() == 1 && bInterfaces.size() == 1) {
            return aInterfaces.iterator().next().compareTo(bInterfaces.iterator().next());
        }
        final var aInterfaceArray = aInterfaces.toArray(new InterfaceDescriptor[0]);
        final var bInterfaceArray = bInterfaces.toArray(new InterfaceDescriptor[0]);
        Arrays.sort(aInterfaceArray);
        Arrays.sort(bInterfaceArray);
        final var i1 = Math.min(aInterfaceArray.length, bInterfaceArray.length);
        for (var i0 = 0; i0 < i1; ++i0) {
            d = aInterfaceArray[i0].compareTo(bInterfaceArray[i0]);
            if (d != 0) {
                return d;
            }
        }
        return aInterfaceArray.length - bInterfaceArray.length;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        final ServiceDescription that = (ServiceDescription) other;
        return version == that.version &&
            name.equals(that.name) &&
            provider.equals(that.provider) &&
            uri.equals(that.uri) &&
            receivedAt.equals(that.receivedAt) &&
            expiresAt.equals(that.expiresAt) &&
            security.equals(that.security) &&
            metadata.equals(that.metadata) &&
            interfaceTokens.equals(that.interfaceTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, provider, uri, receivedAt, expiresAt, security, metadata, version, interfaceTokens);
    }

    @Override
    public String toString() {
        return "ServiceDescription{" +
            "name='" + name + '\'' +
            ", provider=" + provider +
            ", uri='" + uri + '\'' +
            ", receivedAt=" + receivedAt +
            ", expiresAt=" + expiresAt +
            ", security=" + security +
            ", metadata=" + metadata +
            ", version=" + version +
            ", interfaceTokens=" + interfaceTokens +
            '}';
    }

    /**
     * Builder useful for creating {@link ServiceDescription} instances.
     */
    @SuppressWarnings("unused")
    public static class Builder {
        private String name;
        private ProviderDescription provider;
        private Map<InterfaceDescriptor, String> interfaceTokens;
        private String uri;
        private Instant receivedAt;
        private Instant expiresAt;
        private SecurityDescriptor security;
        private Map<String, String> metadata;
        private int version;

        /**
         * Service name. <b>Must be specified.</b>
         *
         * @param name Service name.
         * @return This builder.
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * Description of system providing this service. <b>Must be
         * specified.</b>
         *
         * @param provider Providing system description.
         * @return This builder.
         */
        public Builder provider(final ProviderDescription provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets service URI. <b>Must be specified.</b>
         *
         * @return This builder.
         * @see ServiceDescription#uri()
         */
        public Builder uri(final String uri) {
            this.uri = uri;
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
            this.receivedAt = receivedAt;
            return this;
        }

        /**
         * Sets time at which this service description should be renewed or
         * refreshed.
         *
         * @param renewAt Instant soon after which this service description
         *                should be renewed or refreshed.
         * @return This builder.
         */
        public Builder expiresAt(final Instant renewAt) {
            this.expiresAt = renewAt;
            return this;
        }

        /**
         * Sets security descriptor. <b>Must be specified.</b>
         *
         * @param security Security descriptor.
         * @return This builder.
         */
        public Builder security(final SecurityDescriptor security) {
            this.security = security;
            return this;
        }

        /**
         * Sets service metadata.
         *
         * @param metadata Service metadata.
         * @return This builder.
         */
        public Builder metadata(final Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Sets service version. Defaults to 0.
         *
         * @param version Service version.
         * @return This builder.
         */
        public Builder version(final int version) {
            this.version = version;
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
        public Builder interfaces(final InterfaceDescriptor... interfaces) {
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
        public Builder interfaces(final Collection<InterfaceDescriptor> interfaces) {
            final var interfaceTokens = interfaces.stream()
                .collect(Collectors.toMap(descriptor -> descriptor, ignored -> ""));
            if (this.interfaceTokens != null) {
                this.interfaceTokens.putAll(interfaceTokens);
            }
            else {
                this.interfaceTokens = interfaceTokens;
            }
            return this;
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
        public Builder interfaceTokens(final Map<InterfaceDescriptor, String> interfaceTokens) {
            if (this.interfaceTokens != null) {
                this.interfaceTokens.putAll(interfaceTokens);
            }
            else {
                this.interfaceTokens = interfaceTokens;
            }
            return this;
        }

        /**
         * @return New {@link ServiceDescription}.
         */
        public ServiceDescription build() {
            return new ServiceDescription(this);
        }
    }
}
