package se.arkalix.security.identity;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Holds the Arrowhead certificate chain and private key required to manage an
 * <i>owned</i> system or operator identity.
 * <p>
 * Instances of this class are guaranteed to only hold x.509 certificates
 * complying to the Arrowhead certificate {@link SystemIdentity naming
 * conventions}.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class OwnedIdentity extends SystemIdentity {
    private final PrivateKey privateKey;

    /**
     * Creates new owned identity from given chain of x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity of a system, the
     * certificate at index 1 the identity of a cloud, while all subsequent
     * certificates constitute their chain of issuers. The certificates,
     * including the first two, must be in order in the sense that the
     * certificate at index {@code n} must be issued by the certificate at
     * index {@code n + 1}.
     *
     * @param chain      x.509 certificate chain belonging to an Arrowhead
     *                   system.
     * @param privateKey Private key associated with the system certificate.
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
    public OwnedIdentity(final Certificate[] chain, final PrivateKey privateKey) throws SystemIdentityException {
        super(chain);
        this.privateKey = Objects.requireNonNull(privateKey, "Expected privateKey");
        verify();
    }

    /**
     * Creates new system identity from given chain of x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity of a system, the
     * certificate at index 1 the identity of a cloud, while all subsequent
     * certificates constitute their chain of issuers. The certificates,
     * including the first two, must be in order in the sense that the
     * certificate at index {@code n} must be issued by the certificate at
     * index {@code n + 1}.
     *
     * @param chain      x.509 certificate chain belonging to an Arrowhead
     *                   system.
     * @param privateKey Private key associated with the system certificate.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0} or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final X509Certificate[] chain, final PrivateKey privateKey) {
        super(chain);
        this.privateKey = Objects.requireNonNull(privateKey, "Expected privateKey");
        verify();
    }

    /**
     * Promotes given {@code identity} to an {@link OwnedIdentity}.
     * <p>
     * The promotion will succeed only if the certificate of the given
     * {@code identity} contains an Arrowhead-compliant subject common name.
     *
     * @param identity   Identity to promote.
     * @param privateKey Private key associated with system certificate.
     * @throws IllegalArgumentException If given {@code identity} does not
     *                                  satisfy the Arrowhead naming
     *                                  requirements.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final SystemIdentity identity, final PrivateKey privateKey) {
        super(identity);
        this.privateKey = Objects.requireNonNull(privateKey, "Expected privateKey");
        verify();
    }

    /**
     * Promotes given {@code identity} to an {@link OwnedIdentity}.
     * <p>
     * The promotion will succeed only if the certificate of the given
     * {@code identity} contains an Arrowhead-compliant subject common name.
     *
     * @param identity   Identity to promote.
     * @param privateKey Private key associated with system certificate.
     * @throws IllegalArgumentException If given {@code identity} does not
     *                                  satisfy the Arrowhead naming
     *                                  requirements.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final TrustedIdentity identity, final PrivateKey privateKey) {
        super(identity);
        this.privateKey = Objects.requireNonNull(privateKey, "Expected privateKey");
        verify();
    }

    private void verify() {
        final var cloud = issuer().orElseThrow(() ->
            new IllegalArgumentException("Missing cloud certificate; " +
                "each system certificate must be issued by a cloud " +
                "certificate, or the system in question will not be able " +
                "to interact with any other systems"));

        final String cln;
        try {
            cln = cloud.commonName();
        }
        catch (final IllegalStateException exception) {
            throw new IllegalArgumentException("Cloud certificate does not " +
                "contain a subject common name", exception);
        }

        final var syn = commonName();
        final var off = systemName().length() + 1;
        final var len = Math.min(syn.length() - off, cln.length());
        if (!syn.regionMatches(off, cln, 0, len)) {
            throw new IllegalArgumentException("Cloud certificate common name " +
                "expected to be \"" + syn.substring(off) + "\"; found \"" +
                cln + "\"");
        }
    }

    /**
     * @return Private key associated key store system.
     */
    public PrivateKey privateKey() {
        return privateKey;
    }
}
