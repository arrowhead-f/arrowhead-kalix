package se.arkalix.core.plugin.dto;

import se.arkalix.description.ProviderDescription;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;
import se.arkalix.internal.security.identity.X509Keys;

import java.net.InetSocketAddress;
import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Details about an Arrowhead system, as they appear in various core service
 * messages.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
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
}
