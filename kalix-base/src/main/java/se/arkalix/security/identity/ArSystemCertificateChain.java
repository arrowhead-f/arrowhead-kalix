package se.arkalix.security.identity;

import se.arkalix.internal.net.dns.DnsNames;
import se.arkalix.internal.security.identity.X509Names;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Objects;
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
public class ArSystemCertificateChain {
    private final X509Certificate[] chain;

    private boolean isVerified = false;

    private String systemCommonName = null;
    private String systemName = null;

    private String cloudCommonName = null;
    private String cloudName = null;

    private String companyCommonName = null;
    private String companyName = null;

    private String masterCommonName = null;

    /**
     * Creates new x.509 certificate chain, representing a single Arrowhead
     * system, from given {@code chain}.
     *
     * @param chain x.509 certificate chain. The certificate at index 0 must
     *              belong to an Arrowhead system, and the other indexes must
     *              refer to its issuers, from lowest level up to the root.
     *              More precisely, the certificate at index 1 must be a cloud
     *              certificate, the one at index 2 must be a company
     *              certificate and, finally, the one at index 3 a master
     *              certificate.
     */
    public ArSystemCertificateChain(final X509Certificate[] chain) {
        Objects.requireNonNull(chain, "Expected chain");
        if (chain.length < 4) {
            throw new IllegalArgumentException("Expected chain.length >= 4");
        }

        this.chain = chain;
    }

    /**
     * Attempts to create new x.509 certificate chain, representing a single
     * Arrowhead system, from given {@code chain}.
     * <p>
     * An empty result is returned only if any certificate in the given
     * {@code chain} is of any other type than x.509, or if fewer than 4
     * certificates are provided.
     *
     * @param chain Certificate chain. The certificate at index 0 must belong
     *              to an Arrowhead system, and the other indexes must refer to
     *              its issuers, from lowest level up to the root. More
     *              precisely, the certificate at index 1 must be a cloud
     *              certificate, the one at index 2 must be a company
     *              certificate and, finally, the one at index 3 a master
     *              certificate.
     */
    public static Optional<ArSystemCertificateChain> tryConvert(final Certificate[] chain) {
        Objects.requireNonNull(chain, "Expected chain");
        if (chain.length < 4) {
            return Optional.empty();
        }

        final var x509chain = new X509Certificate[chain.length];
        for (var i = 0; i < chain.length; ++i) {
            final var certificate = chain[i];
            if (!(certificate instanceof X509Certificate)) {
                return Optional.empty();
            }
            x509chain[i] = (X509Certificate) chain[i];
        }

        return Optional.of(new ArSystemCertificateChain(x509chain));
    }

    private IllegalArgumentException commonNameNotDnsName(final String cn, final String type) {
        return new IllegalArgumentException("Certificate CN \"" + cn +
            "\" is not a valid DNS domain name; the certificate is " +
            "assumed to belong to a " + type + ", for which it is " +
            "mandatory");
    }

    private IllegalArgumentException commonNameNotContainingIssuerName(
        final String cn,
        final String type,
        final String issuerCn)
    {
        throw new IllegalArgumentException("Certificate CN is " +
            "not a DNS domain name ending with the name of its " +
            "issuer CN; expected " + type + " CN \"" + cn + "\" " +
            "to end with \"" + issuerCn + "\"");
    }

    /**
     * @return System x.509 certificate.
     */
    public X509Certificate system() {
        return chain[0];
    }

    /**
     * @return Clone of system x.509 certificate chain.
     */
    public X509Certificate[] systemChain() {
        return chain.clone();
    }

    /**
     * @return Full CN of system.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String systemCommonName() {
        if (systemCommonName == null) {
            final var dn = system().getSubjectX500Principal().getName();
            systemCommonName = X509Names.commonNameOf(dn)
                .orElseThrow(() -> new IllegalStateException("No CN in " +
                    "system certificate"));
        }
        return systemCommonName;
    }

    /**
     * @return System certificate name.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String systemName() {
        if (systemName == null) {
            systemName = DnsNames.firstLabel(systemCommonName());
        }
        return systemName;
    }

    /**
     * @return System certificate public key.
     */
    public PublicKey systemPublicKey() {
        return system().getPublicKey();
    }

    /**
     * @return Cloud x.509 certificate.
     */
    public X509Certificate cloud() {
        return chain[1];
    }

    /**
     * @return Clone of cloud x.509 certificate chain.
     */
    public X509Certificate[] cloudChain() {
        return Arrays.copyOfRange(chain, 1, chain.length);
    }

    /**
     * @return Full CN of cloud.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String cloudCommonName() {
        if (cloudCommonName == null) {
            final var dn = cloud().getSubjectX500Principal().getName();
            cloudCommonName = X509Names.commonNameOf(dn)
                .orElseThrow(() -> new IllegalStateException("No CN in " +
                    "cloud certificate"));
        }
        return cloudCommonName;
    }

    /**
     * @return Cloud certificate name.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String cloudName() {
        if (cloudName == null) {
            cloudName = DnsNames.firstLabel(cloudCommonName());
        }
        return cloudName;
    }

    /**
     * @return Cloud certificate public key.
     */
    public PublicKey cloudPublicKey() {
        return cloud().getPublicKey();
    }

    /**
     * @return Company x.509 certificate.
     */
    public X509Certificate company() {
        return chain[2];
    }

    /**
     * @return Clone of company x.509 certificate chain.
     */
    public X509Certificate[] companyChain() {
        return Arrays.copyOfRange(chain, 2, chain.length);
    }

    /**
     * @return Full CN of company.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String companyCommonName() {
        if (companyCommonName == null) {
            final var dn = company().getSubjectX500Principal().getName();
            companyCommonName = X509Names.commonNameOf(dn)
                .orElseThrow(() -> new IllegalStateException("No CN in " +
                    "company certificate"));
        }
        return companyCommonName;
    }

    /**
     * @return Company certificate name.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String companyName() {
        if (companyName == null) {
            companyName = DnsNames.firstLabel(companyCommonName());
        }
        return companyName;
    }

    /**
     * @return Company certificate public key.
     */
    public PublicKey companyPublicKey() {
        return company().getPublicKey();
    }

    /**
     * @return Master x.509 certificate.
     */
    public X509Certificate master() {
        return chain[3];
    }

    /**
     * @return Clone of master x.509 certificate chain.
     */
    public X509Certificate[] masterChain() {
        return Arrays.copyOfRange(chain, 3, chain.length);
    }

    /**
     * @return Full CN of master.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String masterCommonName() {
        if (masterCommonName == null) {
            final var dn = master().getSubjectX500Principal().getName();
            masterCommonName = X509Names.commonNameOf(dn)
                .orElseThrow(() -> new IllegalStateException("No CN in " +
                    "master certificate"));
        }
        return masterCommonName;
    }

    /**
     * @return Master certificate name. Always identical to full master CN.
     * @see ArSystemCertificateChain See class description for more details.
     */
    public String masterName() {
        return masterCommonName();
    }

    /**
     * @return Master certificate public key.
     */
    public PublicKey masterPublicKey() {
        return master().getPublicKey();
    }

    /**
     * Performs a complete verification of system, cloud, company and master
     * names.
     */
    public void verifyNameChain() {
        if (isVerified) {
            return;
        }

        final var syn = systemCommonName();
        if (!DnsNames.isName(syn)) {
            throw commonNameNotDnsName(syn, "system");
        }

        final var cln = cloudCommonName();
        if (!DnsNames.isName(cln)) {
            throw commonNameNotDnsName(cln, "cloud");
        }
        if (!syn.endsWith(cln) || syn.charAt(systemName().length()) != '.') {
            throw commonNameNotContainingIssuerName(syn, "system", cln);
        }

        final var con = companyCommonName();
        if (!DnsNames.isName(con)) {
            throw commonNameNotDnsName(con, "company");
        }
        if (!cln.endsWith(con) || cln.charAt(companyName().length()) != '.') {
            throw commonNameNotContainingIssuerName(cln, "cloud", con);
        }

        final var man = masterCommonName();
        if (!DnsNames.isName(man)) {
            throw commonNameNotDnsName(man, "master");
        }
        if (!con.endsWith(man) || con.charAt(cloudName().length()) != '.') {
            throw commonNameNotContainingIssuerName(con, "company", man);
        }

        isVerified = true;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ArSystemCertificateChain that = (ArSystemCertificateChain) o;
        return Arrays.equals(systemChain(), that.systemChain());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(systemChain());
    }
}
