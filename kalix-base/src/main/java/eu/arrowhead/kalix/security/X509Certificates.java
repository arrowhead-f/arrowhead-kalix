package eu.arrowhead.kalix.security;

import java.security.cert.X509Certificate;

/**
 * Helper functions for extracting information from x.509 certificates that is
 * particularly relevant to Arrowhead Framework.
 */
public class X509Certificates {
    private X509Certificates() {}

    public static String commonNameOf(final X509Certificate certificate) {
        final var name = certificate.getSubjectX500Principal().getName(); // TODO
        return null;
    }

    public static String systemNameOf(final X509Certificate certificate) {
        return null;
    }
}
