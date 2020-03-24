package se.arkalix.security.identity;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Holds the Arrowhead certificate chain and private key required to manage an
 * <i>owned</i> system or operator identity.
 * <p>
 * Instances of this class are guaranteed to only hold x.509 certificates
 * complying to the Arrowhead certificate {@link ArSystemCertificateChain
 * naming conventions}.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class ArSystemKeyStore extends ArSystemCertificateChain {
    private final PrivateKey privateKey;

    /**
     * Creates new Arrowhead key store from provided Arrowhead system
     * certificate chain and private key belonging to the certificate at index
     * 0 in the given chain.
     *
     * @param chain      x.509 certificate chain. The certificate at index 0
     *                   must belong to an Arrowhead system, and the other
     *                   indexes must refer to its issuers, from lowest level
     *                   up to the root. More precisely, the certificate at
     *                   index 1 must be a cloud certificate, the one at index
     *                   2 must be a company certificate and, finally, the one
     *                   at index 3 a master certificate.
     * @param privateKey Private key associated with system certificate.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public ArSystemKeyStore(final X509Certificate[] chain, final PrivateKey privateKey) {
        super(chain);
        this.privateKey = Objects.requireNonNull(privateKey, "Expected privateKey");

        verifyNameChain();
    }

    /**
     * @return Private key associated key store system.
     */
    public PrivateKey privateKey() {
        return privateKey;
    }
}
