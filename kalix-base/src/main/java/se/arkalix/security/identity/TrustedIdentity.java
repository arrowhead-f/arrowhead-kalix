package se.arkalix.security.identity;

import se.arkalix.security.identity._internal.X509Names;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a trusted identity, where trust was established by being able to
 * present a valid <a href="https://tools.ietf.org/html/rfc5280">x.509</a>
 * certificate chain containing at least one trusted issuer.
 * <p>
 * Note that this class, in and of itself, does not guarantee that the
 * certificate chain it holds is known to be correct and trustworthy. That fact
 * is assumed to be established prior to this class being handed any
 * certificates.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class TrustedIdentity {
    protected final X509Certificate[] chain;
    protected final int chainOffset;

    private TrustedIdentity issuer = null;
    private String commonName = null;

    /**
     * Creates new trusted identity from given chain of
     * <a href="https://tools.ietf.org/html/rfc5280">x.509 certificates</a>.
     * <p>
     * The certificate at index 0 must represent the identity in question while
     * all subsequent certificates constitute its chain of issuers. The
     * certificates must be in order in the sense that the certificate at index
     * {@code n} must be issued by the certificate at index {@code n + 1}.
     * <p>
     * The smallest valid chain of certificates contains only a single
     * self-signed certificate. No certificate validation is performed by this
     * constructor, other than ensuring that only x.509 certificates are
     * present, but the provided chain should be complete in the sense
     * that it contains all issuers up to a self-signed certificate.
     *
     * @param chain x.509 certificate chain.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0} or if
     *                                  {@code chain} contains any other type
     *                                  of certificate than
     *                                  {@link X509Certificate}.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public TrustedIdentity(final Certificate[] chain) {
        Objects.requireNonNull(chain, "chain");
        final var minimumChainLength = minimumChainLength();
        if (chain.length < minimumChainLength) {
            throw new IllegalArgumentException("Expected chain.length >= " + minimumChainLength);
        }

        this.chain = new X509Certificate[chain.length];
        for (var i = 0; i < chain.length; ++i) {
            final var certificate = chain[i];
            if (!(certificate instanceof X509Certificate)) {
                throw new IllegalArgumentException("Only x.509 certificates " +
                    "are permitted in TrustedIdentity instances; the " +
                    "following certificate is of some other type: " +
                    certificate);
            }
            this.chain[i] = (X509Certificate) chain[i];
        }

        chainOffset = 0;
    }

    /**
     * Creates new trusted identity from given chain of
     * <a href="https://tools.ietf.org/html/rfc5280">x.509 certificates</a>.
     * <p>
     * The certificate at index 0 must represent the identity in question while
     * all subsequent certificates constitute its chain of issuers. The
     * certificates must be in order in the sense that the certificate at index
     * {@code n} must be issued by the certificate at index {@code n + 1}.
     * <p>
     * The smallest valid chain of certificates contains only a single
     * self-signed certificate. No certificate validation is performed by this
     * constructor, but the provided chain should be complete in the sense
     * that it contains all issuers up to a self-signed certificate.
     *
     * @param chain x.509 certificate chain.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0}.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public TrustedIdentity(final X509Certificate[] chain) {
        this.chain = Objects.requireNonNull(chain, "chain");
        final var minimumChainLength = minimumChainLength();
        if (chain.length < minimumChainLength) {
            throw new IllegalArgumentException("Expected chain.length >= " + minimumChainLength);
        }
        chainOffset = 0;
    }

    protected TrustedIdentity(final X509Certificate[] chain, final int chainOffset) {
        this.chain = chain;
        this.chainOffset = chainOffset;
    }

    protected int minimumChainLength() {
        return 1;
    }

    /**
     * @return Clone of x.509 certificate chain associated with this identity.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509Certificate[] chain() {
        return Arrays.copyOfRange(chain, chainOffset, chain.length);
    }

    /**
     * @return Representation of certificate issuer, if any such is available.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public Optional<TrustedIdentity> issuer() {
        if (issuer == null) {
            final var nextOffset = chainOffset + 1;
            if (chain.length == nextOffset) {
                return Optional.empty();
            }
            issuer = new TrustedIdentity(chain, nextOffset);
        }
        return Optional.of(issuer);
    }

    /**
     * @return x.509 certificate of this identity.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509Certificate certificate() {
        return chain[chainOffset];
    }

    /**
     * Gets subject common name from the x.509 certificate of the trusted
     * identity.
     * <p>
     * Scans the distinguished name of the certificate subject and returns the
     * leftmost such found. It can in most cases be expected to be very rare
     * for a certificate to contain any other number than exactly one such
     * name.
     *
     * @return Subject common name.
     * @throws IllegalStateException If no common name is specified in the
     *                               certificate.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public String commonName() {
        if (commonName == null) {
            final var dn = certificate().getSubjectX500Principal().getName();
            commonName = X509Names.commonNameOf(dn)
                .orElseThrow(() -> new IllegalStateException("Certificate " +
                    "does not specify a subject common name"));
        }
        return commonName;
    }

    /**
     * @return Public key of trusted identity.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public PublicKey publicKey() {
        return certificate().getPublicKey();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) { return true; }
        if (other == null || getClass() != other.getClass()) { return false; }
        TrustedIdentity that = (TrustedIdentity) other;
        return chain[chainOffset].equals(that.chain[that.chainOffset]);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(chain);
    }
}
