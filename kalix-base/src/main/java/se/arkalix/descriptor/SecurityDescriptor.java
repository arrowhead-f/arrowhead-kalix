package se.arkalix.descriptor;

/**
 * Describes security modes in which Arrowhead Systems may operate.
 */
public enum SecurityDescriptor {
    /**
     * No security.
     * <p>
     * No certificates or security identifiers are exchanged while systems
     * interact in this mode.
     */
    NOT_SECURE,

    /**
     * Certificate-only security.
     * <p>
     * System trust is based on whether certificates received from them are
     * signed by trusted issuers and contain white-listed common names.
     */
    CERTIFICATE,

    /**
     * Certificate and token security.
     * <p>
     * System trust is based on whether certificates received from them are
     * signed by trusted issuers and are able to present valid access tokens.
     */
    TOKEN,
}
