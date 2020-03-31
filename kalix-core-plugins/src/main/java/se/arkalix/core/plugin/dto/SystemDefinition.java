package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import java.util.Optional;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * A description of a system, as it appears in a {@link ServiceRegistration}.
 */
@DtoReadableAs(JSON)
@DtoWritableAs(JSON)
public interface SystemDefinition {
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
}
