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
public interface ServiceDescription {
    /**
     * @return Name, or <i>service definition</i>, of this service.
     */
    String name();

    /**
     * Qualifier that distinguishes this service from other such provided by
     * the same system. How it is used depends on the application-level
     * transport protocol employed by the service.
     * <p>
     * For example, if this service uses HTTP, then the qualifier is a URL base
     * path.
     *
     * @return Service qualifier.
     */
    String qualifier();

    /**
     * @return Security schema used to authenticate and authorize service
     * users.
     */
    SecurityDescriptor security();

    /**
     * @return Metadata associated with this service. Their significance and
     * use depend on the service.
     */
     Map<String, String> metadata();

    /**
     * @return Service version.
     */
    int version();

    /**
     * @return Interface triplets supported by the described service. The
     * returned list should be unmodifiable.
     */
    List<InterfaceDescriptor> supportedInterfaces();

    /**
     * Builder useful for creating {@link ServiceDescription} instances.
     */
    class Builder {
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
            Objects.requireNonNull(name, "Expected name");
            Objects.requireNonNull(qualifier, "Expected qualifier");
            Objects.requireNonNull(security, "Expected security");
            final var metadata = this.metadata == null
                ? Collections.<String, String>emptyMap()
                : Collections.unmodifiableMap(this.metadata);
            if (supportedInterfaces == null || supportedInterfaces.size() == 0) {
                throw new IllegalArgumentException("At least one supported interface must be specified");
            }
            final var supportedInterfaces = Collections.unmodifiableList(this.supportedInterfaces);

            return new ServiceDescription() {
                @Override
                public String name() {
                    return name;
                }

                @Override
                public String qualifier() {
                    return qualifier;
                }

                @Override
                public SecurityDescriptor security() {
                    return security;
                }

                @Override
                public Map<String, String> metadata() {
                    return metadata;
                }

                @Override
                public int version() {
                    return version;
                }

                @Override
                public List<InterfaceDescriptor> supportedInterfaces() {
                    return supportedInterfaces;
                }
            };
        }
    }
}
