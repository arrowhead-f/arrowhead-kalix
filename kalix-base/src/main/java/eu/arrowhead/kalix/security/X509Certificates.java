package eu.arrowhead.kalix.security;

import java.security.cert.X509Certificate;

/**
 * Helper functions for extracting information from x.509 certificates that is
 * particularly relevant to the Arrowhead Framework.
 */
public class X509Certificates {
    private X509Certificates() {}

    /**
     * Searches for and extracts the Common Name (CN) from the subject
     * Distinguished Name (DN) from given x.509 {@code certificate}.
     * <p>
     * More exactly, the method first collects the string representation of the
     * subject DN of the provided {@code certificate}, after which it attempts
     * to find the first occurrence of the regular expression {@code CN[ ]*=}
     * (case insensitive) in that string. It then extracts all characters
     * following it up until the first unescaped comma ({@code ,}), plus
     * ({@code +}) or the end of the subject DN. The attribute value is trimmed
     * of any leading or trailing whitespace before being returned. In other
     * words, it is assumed that the DN contains no more than exactly one CN
     * type/value attribute pair, or that only the first encountered such pair
     * is of relevance.
     *
     * @param certificate Certificate from which to extract CN.
     * @return Extracted CN.
     * @see <a href="https://tools.ietf.org/html/rfc4514">RFC 4514</a>
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public static String subjectCommonNameOf(final X509Certificate certificate) throws DistinguishedNameException {
        final var dn = certificate.getSubjectX500Principal().getName();
        return DistinguishedName.commonNameOf(dn);
    }

    public static ArrowheadName subjectArrowheadNameOf(final X509Certificate certificate) throws DistinguishedNameException {
        final var cn = subjectCommonNameOf(certificate);
        return ArrowheadName.fromCommonName(cn);
    }
}
