package se.arkalix.security.identity;

import se.arkalix.internal.net.dns.DnsNames;
import se.arkalix.internal.security.identity.X509Names;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

/**
 * An x.509 certificate chain associated with an Arrowhead system.
 * <p>
 * The Arrowhead Framework mandates that a particular naming schema be used for
 * certificate Common Names (CNs). The naming schema is intended to reflect the
 * following hierarchy:
 * <ol>
 *     <li>SYSTEM / OPERATOR</li>
 *     <li>CLOUD</li>
 *     <li>COMPANY</li>
 *     <li>MASTER</li>
 *     <li>SUPER</li>
 * </ol>
 * Every SYSTEM / OPERATOR certificate <b>must</b> be issued by a CLOUD
 * certificate, which in turn <i>may</i> be issued by either a COMPANY or
 * MASTER certificate. Each COMPANY certificate <i>may</i> be issued by a
 * MASTER, which in turn may be issued by one or more SUPER certificates.
 * <p>
 * Excluding the SUPER level, all other certificate types must contain a single
 * Common Name (CN), part of its subject LDAP Distinguished Name, that is a DNS
 * domain name containing the entirety of its issuer CN. If, for example, a
 * MASTER certificate would contain the CN "arrowhead.eu", then must the CNs of
 * all COMPANY and/or CLOUD certificates issued by that MASTER end with
 * ".arrowhead.eu".
 * <p>
 * Looking at an example SYSTEM CN, the names of the first four levels of the
 * hierarchy become apparent:
 * <pre>
 *     system-14.cloud-1.the-company.arrowhead.eu
 *     |_______| |_____| |_________| |__________|
 *         |        |         |           |
 *       SYSTEM   CLOUD    COMPANY      MASTER
 * </pre>
 * The CN of the <i>cloud-1</i> certificate would be
 * "cloud-1.the-company.arrowhead.eu", the CN of the COMPANY would be
 * "the-company.arrowhead.eu" and, finally, the CN of the MASTER would be
 * "arrowhead.eu". As it is not always relevant to use the full CNs when
 * referring to systems, clouds, and so on, these components are also referred
 * to by their so-called <i>names</i>. The name of the cloud in the above
 * example would simply be "cloud-1", while the name of the system would be
 * "system-14". SYSTEM, CLOUD and COMPANY names must consist <b>only</b> of a
 * single DNS name label, which means that they must consist solely of the
 * characters {@code 0-9 A-Z a-z} and {@code -}. A label may not begin with a
 * digit or hyphen, and may not end with a hyphen. The same applies for the
 * MASTER name, with the exception that it may consist of multiple labels
 * separated by dots.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
 * @see <a href="https://tools.ietf.org/html/rfc4512#section-1.4">RFC 4512, Section 1.4</a>
 * @see <a href="https://tools.ietf.org/html/rfc4514#section-3">RFC 4515, Section 3</a>
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class SystemIdentity extends TrustedIdentity {
    private final String systemName;
    private final String cloudName;
    private final String companyName;
    private final String masterName;

    private boolean isVerified = false;

    /**
     * Creates new system identity from given chain of x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity in question while
     * all subsequent certificates constitute its chain of issuers. The
     * certificates must be in order in the sense that the certificate at index
     * {@code n} must be issued by the certificate at index {@code n + 1}.
     * <p>
     * The smallest valid chain of certificates contains only two certificates,
     * a system certificate and its issuer cloud certificate. If this
     * constructor completes successfully, the system certificate has been
     * verified to contain a correct Arrowhead system name. The cloud
     * certificate is not ensured to be present or valid.
     *
     * @param chain x.509 certificate chain belonging to an Arrowhead system.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0}, if
     *                                  {@code chain} contains any other type
     *                                  of certificate than
     *                                  {@link X509Certificate}, or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see SystemIdentity Class description for details on valid names.
     */
    public SystemIdentity(final Certificate[] chain) {
        super(chain);
        final var names = resolveNames();
        systemName = names[0];
        cloudName = names[1];
        companyName = names[2];
        masterName = names[3];
    }

    /**
     * Creates new system identity from given chain of x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity in question while
     * all subsequent certificates constitute its chain of issuers. The
     * certificates must be in order in the sense that the certificate at index
     * {@code n} must be issued by the certificate at index {@code n + 1}.
     * <p>
     * The smallest valid chain of certificates contains only two certificates,
     * a system certificate and its issuer cloud certificate. If this
     * constructor completes successfully, the system certificate has been
     * verified to contain a correct Arrowhead system name. The cloud
     * certificate is not ensured to be present or valid.
     *
     * @param chain x.509 certificate chain belonging to an Arrowhead system.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0} or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see SystemIdentity Class description for details on valid names.
     */
    public SystemIdentity(final X509Certificate[] chain) {
        super(chain);
        final var names = resolveNames();
        systemName = names[0];
        cloudName = names[1];
        companyName = names[2];
        masterName = names[3];
    }

    /**
     * Promotes given {@code identity} to a {@link SystemIdentity}.
     * <p>
     * The promotion will succeed only if the certificate of the given
     * {@code identity} contains an Arrowhead-compliant subject common name.
     *
     * @param identity Identity to promote.
     * @throws IllegalArgumentException If given {@code identity} does not
     *                                  satisfy the Arrowhead naming
     *                                  requirements.
     * @see SystemIdentity Class description for details on valid names.
     */
    public SystemIdentity(final TrustedIdentity identity) {
        super(identity.chain, 0);
        final var names = resolveNames();
        systemName = names[0];
        cloudName = names[1];
        companyName = names[2];
        masterName = names[3];
    }

    private String[] resolveNames() {
        final var commonName = commonName();
        final List<String> names;
        try {
            names = DnsNames.splitName(commonName, 4);
        }
        catch (final IllegalArgumentException exception) {
            throw new IllegalArgumentException("The common name \"" +
                commonName + "\" of the provided identity is not a valid " +
                "DNS name", exception);
        }
        if (names.size() != 4) {
            throw new IllegalArgumentException("The common name \"" +
                commonName + "\" of the provided identity is not a valid " +
                "Arrowhead system certificate name; expected it to be on " +
                "the form \"<system>.<cloud>.<company>.<master>\", where " +
                "each part except the last consists of a single DNS label");
        }
        return names.toArray(new String[0]);
    }

    protected SystemIdentity(final SystemIdentity identity) {
        super(identity.chain, identity.chainOffset);
        systemName = identity.systemName;
        cloudName = identity.cloudName;
        companyName = identity.companyName;
        masterName = identity.masterName;
        isVerified = identity.isVerified;
    }

    /**
     * Attempts to create new system identity from given chain of
     * x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity in question while
     * all subsequent certificates constitute its chain of issuers. The
     * certificates must be in order in the sense that the certificate at index
     * {@code n} must be issued by the certificate at index {@code n + 1}.
     * <p>
     * The smallest valid chain of certificates contains only two certificates,
     * a system certificate and its issuer cloud certificate. If this
     * constructor completes successfully, the system certificate has been
     * verified to contain a correct Arrowhead system name. The cloud
     * certificate is ensured to be present, but not valid.
     *
     * @param chain x.509 certificate chain belonging to an Arrowhead system.
     * @return System identity only if given {@code chain} satisfies all
     * criteria.
     * @see SystemIdentity Class description for details on valid names.
     */
    public static Optional<SystemIdentity> tryFrom(final Certificate[] chain) {
        if (chain == null) {
            return Optional.empty();
        }

        final var x509Chain = new X509Certificate[chain.length];
        for (var i = 0; i < chain.length; ++i) {
            final var certificate = chain[i];
            if (!(certificate instanceof X509Certificate)) {
                return Optional.empty();
            }
            x509Chain[i] = (X509Certificate) chain[i];
        }

        if (chain.length < 2) {
            return Optional.empty();
        }

        final var dn = x509Chain[0].getSubjectX500Principal().getName();
        final var cn = X509Names.commonNameOf(dn);
        if (cn.isEmpty()) {
            return Optional.empty();
        }

        final List<String> names;
        try {
            names = DnsNames.splitName(cn.get(), 4);
        }
        catch (final IllegalArgumentException ignored) {
            return Optional.empty();
        }

        if (names.size() != 4) {
            return Optional.empty();
        }

        return Optional.of(new SystemIdentity(x509Chain, names.toArray(new String[0])));
    }

    private SystemIdentity(final X509Certificate[] chain, final String[] names) {
        super(chain, 0);
        systemName = names[0];
        cloudName = names[1];
        companyName = names[2];
        masterName = names[3];
    }

    /**
     * @return System certificate name.
     * @see SystemIdentity See class description for more details.
     */
    public String systemName() {
        return systemName;
    }

    /**
     * @return Cloud certificate name.
     * @see SystemIdentity See class description for more details.
     */
    public String cloudName() {
        return cloudName;
    }

    /**
     * @return Company certificate name.
     * @see SystemIdentity See class description for more details.
     */
    public String companyName() {
        return companyName;
    }

    /**
     * @return Master certificate name. Always identical to full master CN.
     * @see SystemIdentity See class description for more details.
     */
    public String masterName() {
        return masterName;
    }
}
