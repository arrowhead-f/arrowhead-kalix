package se.arkalix.security.identity;

/**
 * Identifies the type of some {@link ArCertificate}.
 */
public enum ArCertificateType {
    /**
     * A system or operator certificate.
     * <p>
     * Must be issued by a {@link #CLOUD} certificate.
     */
    SYSTEM,

    /**
     * A local cloud certificate.
     * <p>
     * Must be issued by a {@link #COMPANY} certificate.
     */
    CLOUD,

    /**
     * A company certificate.
     * <p>
     * Must be issued by a {@link #MASTER} certificate.
     */
    COMPANY,

    /**
     * A master certificate.
     * <p>
     * Might be issued by one or more {@link #SUPER} certificates.
     */
    MASTER,

    /**
     * A certificate used to issue a master certificate or some other super
     * certificate.
     */
    SUPER,
    ;

    /**
     * Determines the appropriate certificate type for the given certificate
     * {@code level}.
     * <p>
     * The levels correspond to the positions certificates of the different
     * types would have in the certificate chain of a SYSTEM, which means that
     * 0 is SYSTEM, 1 is CLOUD, 2 is COMPANY, 3 is MASTER and 4 and beyond are
     * SUPER.
     *
     * @param level Certificate level to resolve.
     * @return Arrowhead certificate type.
     */
    public static ArCertificateType level(final int level) {
        switch (level) {
        case 0: return SYSTEM;
        case 1: return CLOUD;
        case 2: return COMPANY;
        case 3: return MASTER;
        default: return SUPER;
        }
    }
}
