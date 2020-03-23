package se.arkalix.security.identity;

import se.arkalix.internal.net.dns.DnsNames;
import se.arkalix.internal.security.identity.x509.X509Names;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * An Arrowhead x.509 certificate and the certificates of its known issuers.
 * <p>
 * The Arrowhead Framework mandates that a particular naming schema be used for
 * certificate Common Names (CNs). The naming schema is intended to reflect the
 * following hierarchy, enumerated by the {@link ArCertificateType}:
 * <ol>
 *     <li>SYSTEM / OPERATOR</li>
 *     <li>CLOUD</li>
 *     <li>COMPANY</li>
 *     <li>MASTER</li>
 *     <li>SUPER</li>
 * </ol>
 * Every SYSTEM / OPERATOR certificate must be issued by a CLOUD certificate,
 * which in turn must be issued by a COMPANY certificate, which, finally, must
 * be issued by a MASTER certificate. The MASTER certificate may or may not be
 * issued by one or more SUPER certificates.
 * <p>
 * Excluding the SUPER level, all other certificate types must contain a single
 * Common Name (CN), part of its subject LDAP Distinguished Name, that is a DNS
 * domain name containing the entirety of its issuer CN. If, for example, a
 * MASTER certificate would contain the CN "arrowhead.eu", then must the CNs of
 * all COMPANY certificates issued by that MASTER end with ".arrowhead.eu".
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
 * "arrowhead.eu". As it is not always relevant to use the full CN when
 * referring to systems, clouds, and so on, these components are also referred
 * to by their so-called <i>names</i>. The name of the cloud in the above
 * example would simply be "cloud-1", while the name of the system would be
 * "system-14".
 * <p>
 * Each instance of this class can be queried for its name, its type and its
 * issuers. Note that the name will not be a full CN if the type of the
 * certificate is SYSTEM, CLOUD or COMPANY.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
 * @see <a href="https://tools.ietf.org/html/rfc4512#section-1.4">RFC 4512, Section 1.4</a>
 * @see <a href="https://tools.ietf.org/html/rfc4514#section-3">RFC 4515, Section 3</a>
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class ArCertificate {
    private final X509Certificate[] chain;
    private final int chainOffset;
    private final ArCertificateType type;
    private final ArCertificate issuer;
    private final String name;

    private ArPublicKey publicKey = null;
    private X509Certificate[] subChain = null;

    /**
     * Creates new Arrowhead x.509 certificate from given {@code
     * certificateChain}.
     *
     * @param certificateChain x.509 certificate chain. The certificate at
     *                         index 0 must belong to an Arrowhead system, and
     *                         the other indexes must refer to its issuers,
     *                         from lowest level to the root.
     * @return New Arrowhead x.509 certificate.
     */
    public static ArCertificate from(final X509Certificate[] certificateChain) {
        Objects.requireNonNull(certificateChain, "Expected certificateChain");
        if (certificateChain.length == 0) {
            throw new IllegalArgumentException("Expected certificateChain.length > 0");
        }
        return new ArCertificate(certificateChain, 0);
    }

    private ArCertificate(final X509Certificate[] chain, final int chainOffset) {
        this.chain = chain;
        this.chainOffset = chainOffset;

        final var dn = chain[chainOffset].getSubjectX500Principal().getName();
        final var cn = X509Names.commonNameOf(dn)
            .orElseThrow(() -> new IllegalArgumentException("No CN in root " +
                "certificate"));

        type = ArCertificateType.level(chainOffset);

        if (type != ArCertificateType.SUPER && !DnsNames.isValid(cn)) {
            throw new IllegalArgumentException("Certificate CN \"" + cn +
                "\" is not a valid DNS domain name; the certificate is " +
                "assumed to belong to a " + type + ", for which it is " +
                "mandatory");
        }

        final var nextOffset = chainOffset + 1;
        if (nextOffset > chain.length) {
            issuer = new ArCertificate(chain, nextOffset);

            if (type != ArCertificateType.MASTER && type != ArCertificateType.SUPER) {
                final var cn1 = cn.length() - issuer.name.length();
                if (!cn.endsWith(issuer.name) || cn.charAt(cn1) != '.') {
                    throw new IllegalArgumentException("Certificate CN is " +
                        "not a DNS domain name ending with the name of its " +
                        "issuer CN; expected " + type + " CN \"" + cn + "\" " +
                        "to end with \"" + issuer.name + "\"");
                }
                name = cn.substring(0, cn1);
            }
            else {
                name = cn;
            }
        }
        else {
            issuer = null;
            name = cn;
        }
    }

    /**
     * @return Java 1.2 x.509 certificate chain, including this certificate and
     * all of its issuers.
     */
    public X509Certificate[] toX509CertificateChain() {
        if (subChain == null) {
            subChain = Arrays.copyOfRange(chain, chainOffset, chain.length);
        }
        return subChain;
    }

    /**
     * @return The issuer of this certificate, if any.
     */
    public Optional<ArCertificate> issuer() {
        return Optional.ofNullable(issuer);
    }

    /**
     * @return Arrowhead certificate type.
     */
    public ArCertificateType type() {
        return type;
    }

    /**
     * @return Certificate name.
     * @see ArCertificate See class description for more details.
     */
    public String name() {
        return name;
    }

    /**
     * @return Certificate public key.
     */
    public ArPublicKey publicKey() {
        if (publicKey == null) {
            publicKey = ArPublicKey.of(chain[chainOffset].getPublicKey());
        }
        return publicKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ArCertificate that = (ArCertificate) o;
        return Arrays.equals(toX509CertificateChain(), that.toX509CertificateChain());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toX509CertificateChain());
    }
}
