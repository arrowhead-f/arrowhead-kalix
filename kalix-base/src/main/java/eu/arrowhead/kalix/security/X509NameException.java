package eu.arrowhead.kalix.security;

/**
 * Represents the failure to parse an LDAP DN, potentially associated with some
 * x.509 certificate.
 */
public class X509NameException extends IllegalArgumentException {
    /**
     * Creates new name exception.
     *
     * @param message Description of offence.
     * @param dn      Offending DN.
     * @param offset  Position of offending character in DN.
     */
    public X509NameException(final String message, final String dn, final int offset) {
        super(message + " at offset " + offset + " in \"" + dn + "\"");
    }
}
