package se.arkalix.core.plugin.dto;

import se.arkalix.dto.DtoToString;
import se.arkalix.dto.DtoWritableAs;
import se.arkalix.dto.json.JsonName;

import static se.arkalix.dto.DtoEncoding.JSON;

/**
 * Details about an Arrowhead local cloud, as they appear in various core
 * service messages.
 */
@DtoWritableAs(JSON)
@DtoToString
public interface CloudDetails {
    /**
     * Cloud identifier.
     */
    @JsonName("name")
    String name();

    /**
     * Cloud company identifier.
     */
    @JsonName("operator")
    String company();

    /**
     * Whether or not this cloud operates in secure mode.
     */
    @JsonName("secure")
    Boolean isSecure();

    /**
     * Whether or not this cloud is a so-called neighbor cloud of the cloud to
     * which the sender or receiver of this message belongs to.
     */
    @JsonName("neighbor")
    Boolean isNeighbor();

    /**
     * The public key of what? The service registry? The gatekeeper?
     */
    @JsonName("authenticationInfo")
    String publicKeyBase64();
}
