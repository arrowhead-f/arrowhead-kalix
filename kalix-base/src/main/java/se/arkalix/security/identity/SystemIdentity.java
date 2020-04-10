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
 * Instances of this class are guaranteed to only hold x.509 certificates
 * complying to the Arrowhead certificate {@link se.arkalix.security.identity
 * naming conventions}.
 *
 * @see se.arkalix.security.identity Arrowhead Identity Management
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
     * @throws IllegalArgumentException If {@code chain.length < 2}, if
     *                                  {@code chain} contains any other type
     *                                  of certificate than
     *                                  {@link X509Certificate}, or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see se.arkalix.security.identity Arrowhead Identity Management
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
     * @throws IllegalArgumentException If {@code chain.length < 2} or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see se.arkalix.security.identity Arrowhead Identity Management
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
     * {@code identity} contains an Arrowhead-compliant subject common name and
     * has a cloud certificate in its certificate chain.
     *
     * @param identity Identity to promote.
     * @throws IllegalArgumentException If given {@code identity} does not
     *                                  satisfy the requirements for being a
     *                                  system identity.
     * @see se.arkalix.security.identity Arrowhead Identity Management
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

    @Override
    protected int minimumChainLength() {
        return 2;
    }

    /**
     * Attempts to create new system identity from given chain of certificates.
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
     * @see se.arkalix.security.identity Arrowhead Identity Management
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
     * @return System name.
     * @see se.arkalix.security.identity Arrowhead Identity Management
     */
    public String name() {
        return systemName;
    }

    /**
     * @return Cloud identity.
     */
    public TrustedIdentity cloud() {
        return issuer().orElseThrow(() -> new IllegalStateException("No " +
            "cloud certificate available; this should be impossible"));
    }

    /**
     * @return Cloud name.
     * @see se.arkalix.security.identity Arrowhead Identity Management
     */
    public String cloudName() {
        return cloudName;
    }

    /**
     * @return Company name.
     * @see se.arkalix.security.identity Arrowhead Identity Management
     */
    public String companyName() {
        return companyName;
    }

    /**
     * @return Master name. Always identical to full master CN.
     * @see se.arkalix.security.identity Arrowhead Identity Management
     */
    public String masterName() {
        return masterName;
    }
}
