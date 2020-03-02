package eu.arrowhead.kalix.security;

import eu.arrowhead.kalix.descriptor.CertificateDescriptor;

import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Helper functions for extracting information from x.509 certificates that is
 * particularly relevant to the Arrowhead Framework.
 */
public class X509Certificates {
    private X509Certificates() {}

    /**
     * Searches for and extracts the leftmost Common Name (CN) from the subject
     * Distinguished Name (DN) from given x.509 {@code certificate}, which is
     * then converted into an Arrowhead {@link CertificateDescriptor}.
     * <p>
     * The current evaluation version of Arrowhead Framework demands that only
     * one CN is stated in the subject field of an x.509 certificate, and that
     * it contains a so-called Arrowhead certificate descriptor, which is a
     * four-part domain name consisting of (1) a system or operator identifier,
     * (2) a cloud identifier, (3) a company identifier and (4) a master
     * certificate identifier, which must be "arrowhead.eu".
     * <p>
     * While it is currently common for Internet servers to use the subject CN
     * attribute to tie their x.509 certificates to particular domain names,
     * the practice was deprecated already in RFC 2818, which requires that the
     * {@code SubjectAlternativeName} extension be used for that purpose
     * instead. This frees up the CN for other uses, which is taken advantage
     * of by the Arrowhead Framework.
     *
     * @param certificate Certificate from which to extract Arrowhead
     *                    certificate descriptor.
     * @return Arrowhead certificate descriptor, if a valid CN is present in
     * subject DN string
     * of the given {@code certificate}.
     * @throws IllegalArgumentException If certificate contains a subject CN,
     *                                  but it is not a valid Arrowhead
     *                                  certificate descriptor.
     * @throws X509NameException        If certificate contains a DN string
     *                                  that is not standards-compliant.
     * @see <a href="https://tools.ietf.org/html/rfc2818#section-3.1">RFC 2818, Section 3.1</a>
     * @see <a href="https://tools.ietf.org/html/rfc4514#section-3">RFC 4515, Section 3</a>
     * @see <a href="https://tools.ietf.org/html/rfc4512#section-1.4">RFC 4512, Section 1.4</a>
     */
    public static Optional<CertificateDescriptor> subjectDescriptorOf(final X509Certificate certificate) {
        final var dn = certificate.getSubjectX500Principal().getName();
        return X509Names.certificateDescriptorOf(dn);
    }

    /**
     * Searches for and extracts the leftmost Common Name (CN) from the subject
     * Distinguished Name (DN) from given x.509 {@code certificate}.
     *
     * @param certificate Certificate from which to extract CN.
     * @return Extracted CN, if a valid such is present in the subject DN
     * string of the given {@code certificate}.
     * @throws X509NameException If provided DN string is not standards-
     *                           compliant.
     * @see <a href="https://tools.ietf.org/html/rfc4514">RFC 4514</a>
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public static Optional<String> subjectCommonNameOf(final X509Certificate certificate) {
        final var dn = certificate.getSubjectX500Principal().getName();
        return X509Names.commonNameOf(dn);
    }
}
