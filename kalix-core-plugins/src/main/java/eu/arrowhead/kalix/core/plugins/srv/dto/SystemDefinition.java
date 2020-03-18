package eu.arrowhead.kalix.core.plugins.srv.dto;

import eu.arrowhead.kalix.dto.Writable;
import eu.arrowhead.kalix.dto.json.JsonName;

import java.util.Optional;

/**
 * A description of a system, as it appears in a {@link ServiceRegistration}.
 */
@Writable
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
