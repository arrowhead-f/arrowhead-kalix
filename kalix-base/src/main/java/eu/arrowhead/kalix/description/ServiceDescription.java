package eu.arrowhead.kalix.description;

import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.SecurityDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Describes an Arrowhead Framework service.
 */
public class ServiceDescription {
    private final String name;
    private final List<InterfaceDescriptor> supportedInterfaces;
    private final String qualifier;
    private final SecurityDescriptor security;
    private final Map<String, String> metadata;
    private final int version;

    private ServiceDescription(final Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Expected name");
        if (builder.supportedInterfaces == null || builder.supportedInterfaces.size() == 0) {
            throw new IllegalArgumentException("At least one supported interface must be specified");
        }
        this.supportedInterfaces = Collections.unmodifiableList(builder.supportedInterfaces);
        this.qualifier = Objects.requireNonNull(builder.qualifier, "Expected qualifier");
        this.security = Objects.requireNonNull(builder.security, "Expected security");
        this.metadata = builder.metadata == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(builder.metadata);
        this.version = builder.version;
    }

    /**
     * @return Name, or <i>service definition</i>, of this service.
     */
    public String name() {
        return name;
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
    public String qualifier() {
        return qualifier;
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
     * returned list should be unmodifiable.
     */
    public List<InterfaceDescriptor> supportedInterfaces() {
        return supportedInterfaces;
    }

    /**
     * Builder useful for creating {@link ServiceDescription} instances.
     */
    public static class Builder {
        private String name;
        private List<InterfaceDescriptor> supportedInterfaces;
        private String qualifier;
        private SecurityDescriptor security;
        private Map<String, String> metadata;
        private int version;

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder qualifier(final String qualifier) {
            this.qualifier = qualifier;
            return this;
        }

        public Builder security(final SecurityDescriptor security) {
            this.security = security;
            return this;
        }

        public Builder metadata(final Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder version(final int version) {
            this.version = version;
            return this;
        }

        public Builder supportedInterfaces(final List<InterfaceDescriptor> supportedInterfaces) {
            this.supportedInterfaces = supportedInterfaces;
            return this;
        }

        public ServiceDescription build() {
            return new ServiceDescription(this);
        }
    }
}
