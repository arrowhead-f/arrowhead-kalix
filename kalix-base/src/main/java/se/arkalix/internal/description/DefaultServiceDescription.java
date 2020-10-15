package se.arkalix.internal.description;

import se.arkalix.description.ServiceDescription;
import se.arkalix.description.SystemDescription;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.internal.net.dns.DnsNames;
import se.arkalix.util.annotation.Internal;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Internal
public class DefaultServiceDescription implements ServiceDescription {
    private final String name;
    private final SystemDescription provider;
    private final String uri;
    private final Instant receivedAt;
    private final Instant expiresAt;
    private final SecurityDescriptor security;
    private final Map<String, String> metadata;
    private final int version;
    private final Map<InterfaceDescriptor, String> interfaceTokens;

    private DefaultServiceDescription(final Builder builder) {
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

    @Override
    public String name() {
        return name;
    }

    @Override
    public SystemDescription provider() {
        return provider;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public Instant receivedAt() {
        return receivedAt;
    }

    @Override
    public Instant expiresAt() {
        return expiresAt;
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
        return version == that.version() &&
            name.equals(that.name()) &&
            provider.equals(that.provider()) &&
            uri.equals(that.uri()) &&
            receivedAt.equals(that.receivedAt()) &&
            expiresAt.equals(that.expiresAt()) &&
            security.equals(that.security()) &&
            metadata.equals(that.metadata()) &&
            interfaceTokens.equals(that.interfaceTokens());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, provider, uri, receivedAt, expiresAt, security, metadata, version, interfaceTokens);
    }

    @Override
    public String toString() {
        return "DefaultServiceDescription{" +
            "name='" + name + '\'' +
            ", provider=" + provider +
            ", uri='" + uri + '\'' +
            ", receivedAt=" + receivedAt +
            ", expiresAt=" + expiresAt +
            ", security=" + security +
            ", metadata=" + metadata +
            ", version=" + version +
            ", interfaceTokens=" + stringifyTokens() +
            '}';
    }

    private String stringifyTokens() {
        return interfaceTokens.entrySet()
            .stream()
            .map(entry -> {
                final var token = entry.getValue();
                return entry.getKey() + "='" + token.substring(0, Math.min(token.length(), 4)) + "...'";
            })
            .collect(Collectors.joining(", ", "[", "]"));
    }

    public static class Builder {
        private String name;
        private SystemDescription provider;
        private Map<InterfaceDescriptor, String> interfaceTokens;
        private String uri;
        private Instant receivedAt;
        private Instant expiresAt;
        private SecurityDescriptor security;
        private Map<String, String> metadata;
        private int version;

        public void name(final String name) {
            this.name = name;
        }

        public void provider(final SystemDescription provider) {
            this.provider = provider;
        }

        public void uri(final String uri) {
            this.uri = uri;
        }

        public void receivedAt(final Instant receivedAt) {
            this.receivedAt = receivedAt;
        }

        public void expiresAt(final Instant renewAt) {
            this.expiresAt = renewAt;
        }

        public void security(final SecurityDescriptor security) {
            this.security = security;
        }

        public void metadata(final Map<String, String> metadata) {
            this.metadata = metadata;
        }

        public void version(final int version) {
            this.version = version;
        }

        public void interfaceTokens(final Map<InterfaceDescriptor, String> interfaceTokens) {
            this.interfaceTokens = interfaceTokens;
        }

        public DefaultServiceDescription build() {
            return new DefaultServiceDescription(this);
        }
    }
}
