package se.arkalix.core.plugin;

import se.arkalix.ArSystem;
import se.arkalix.SystemRecord;
import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoReadableAs;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;
import se.arkalix.security.identity._internal.X509Keys;

import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

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
     *
     * @return System name.
     */
    @DtoJsonName("systemName")
    String name();

    /**
     * Hostname or textual representation of IP address of system.
     *
     * @return System hostname or address.
     */
    @DtoJsonName("address")
    String hostname();

    /**
     * Port number.
     *
     * @return Port number.
     */
    int port();

    /**
     * System metadata. Their significance and use depend on the system.
     *
     * @return System metadata.
     */
    Map<String, String> metadata();

    /**
     * Public key of system, if running in secure mode.
     * <p>
     * Must be the Base64 encoded variant of a DER-encoded PKCS#8 private key.
     *
     * @return System public key, if any.
     */
    @DtoJsonName("authenticationInfo")
    Optional<String> publicKeyBase64();

    /**
     * Converts this objects into a {@link SystemRecord}.
     *
     * @return New {@link SystemRecord}.
     * @throws RuntimeException If the value returned by {@link
     *                          #publicKeyBase64()} is not a supported type of
     *                          public key.
     */
    default SystemRecord toSystemDescription() {
        return SystemRecord.from(name(), publicKeyBase64()
            .map(X509Keys::parsePublicKey)
            .orElse(null), new InetSocketAddress(hostname(), port()), metadata());
    }

    /**
     * Creates new system record from given concrete system instance.
     *
     * @param system System from which to create a record.
     * @return Created system record.
     */
    static SystemDetailsDto from(final ArSystem system) {
        return new SystemDetailsDto.Builder()
            .name(system.name())
            .hostname(system.socketAddress().getHostString())
            .port(system.port())
            .publicKeyBase64(system.isSecure()
                ? Base64.getEncoder().encodeToString(system.identity().publicKey().getEncoded())
                : null)
            .build();
    }

    /**
     * Creates new system record from the given system record.
     *
     * @param provider System record from which to create a new record.
     * @return Creates system record.
     */
    static SystemDetailsDto from(final SystemRecord provider) {
        final var socketAddress = provider.socketAddress();
        return new SystemDetailsDto.Builder()
            .name(provider.name())
            .hostname(socketAddress.getHostString())
            .port(socketAddress.getPort())
            .metadata(provider.metadata())
            .publicKeyBase64(provider.isSecure()
                ? Base64.getEncoder().encodeToString(provider.publicKey().getEncoded())
                : null)
            .build();
    }
}
