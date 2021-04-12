package se.arkalix.core.plugin;

import se.arkalix.dto.DtoEqualsHashCode;
import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.DtoJsonName;

import java.util.Optional;

import static se.arkalix.dto.DtoCodec.JSON;

/**
 * Details about an Arrowhead local cloud, as they appear in various core
 * service messages.
 */
@DtoWritableAs(JSON)
@DtoEqualsHashCode
@DtoToString
public interface CloudDetails {
    /**
     * Cloud identifier.
     *
     * @return Cloud identifier.
     */
    @DtoJsonName("name")
    String name();

    /**
     * Cloud company identifier.
     *
     * @return Cloud company identifier.
     */
    @DtoJsonName("operator")
    String company();

    /**
     * Whether or not this cloud operates in secure mode.
     *
     * @return {@code true} only if this cloud operates in secure mode.
     */
    @DtoJsonName("secure")
    Boolean isSecure();

    /**
     * Whether or not this cloud is a so-called neighbor cloud of the cloud to
     * which the sender or receiver of this message belongs to.
     *
     * @return {@code true} only if this cloud is a neighbor cloud.
     */
    @DtoJsonName("neighbor")
    Boolean isNeighbor();

    /**
     * The public key of what? The service registry? The gatekeeper?
     *
     * @return Public key of something as a Base64-encoded string.
     */
    @DtoJsonName("authenticationInfo")
    Optional<String> publicKeyBase64();
}
