package se.arkalix.core.plugin;

import se.arkalix.ArSystem;
import se.arkalix.description.ProviderDescription;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;
import se.arkalix.internal.security.identity.X509Keys;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Details about an Arrowhead system, as they appear in various core service
 * messages.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface SystemDetails {
    /**
     * Name of system.
     */
    @JsonName("systemName")
    String name();

    /**
     * Hostname or textual representation of IP address of system.
     */
    @JsonName("address")
    String hostname();

    /**
     * Port number.
     */
    int port();

    /**
     * Public key of system, if running in secure mode.
     * <p>
     * Must be the Base64 encoded variant of a DER-encoded PKCS#8 private key.
     */
    @JsonName("authenticationInfo")
    Optional<String> publicKeyBase64();

    /**
     * Converts this objects into a {@link ProviderDescription}.
     *
     * @return New {@link ProviderDescription}.
     * @throws RuntimeException If the value returned by {@link
     *                          #publicKeyBase64()} is not a supported type of
     *                          public key.
     */
    default ProviderDescription toProviderDescription() {
        return new ProviderDescription(name(), new InetSocketAddress(hostname(), port()), publicKeyBase64()
            .map(X509Keys::parsePublicKey)
            .orElse(null));
    }

    static SystemDetailsDto from(final ArSystem system) {
        return new SystemDetailsBuilder()
            .name(system.name())
            .hostname(system.localSocketAddress().getHostString())
            .port(system.localPort())
            .publicKeyBase64(system.isSecure() ?
                Base64.getEncoder().encodeToString(system.identity().publicKey().getEncoded()) : null)
            .build();
    }

    static SystemDetailsDto from(final ProviderDescription provider) {
        final var socketAddress = provider.socketAddress();
        return new SystemDetailsBuilder()
            .name(provider.name())
            .hostname(socketAddress.getHostString())
            .port(socketAddress.getPort())
            .publicKeyBase64(Base64.getEncoder().encodeToString(provider.publicKey().getEncoded()))
            .build();
    }
}
