package eu.arrowhead.kalix.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a collection of trusted PEM encoded x.509 certificates.
 *
 * @see <a href="https://tools.ietf.org/html/rfc1421">RFC 1421</a>
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class TruststorePEM implements Truststore {
    private final List<byte[]> trustedCertificates;

    /**
     * Creates new {@link TruststorePEM} from a list of PEM encoded x.509
     * certificates. Note that each byte array may contain any number of
     * certificates.
     *
     * @param trustedCertificates List of byte arrays containing PEM encoded
     *                            x.509 certificates.
     */
    public TruststorePEM(final List<byte[]> trustedCertificates) {
        this.trustedCertificates = trustedCertificates;
    }

    /**
     * Creates new {@link TruststorePEM} from a list of PEM encoded x.509
     * certificates. Note that each byte array may contain any number of
     * certificates.
     *
     * @param trustedCertificates Array of byte arrays containing PEM encoded
     *                            x.509 certificates.
     */
    public TruststorePEM(final byte[]... trustedCertificates) {
        this(Arrays.asList(trustedCertificates));
    }

    /**
     * Creates new {@link TruststorePEM} by reading PEM encoded x.509
     * certificates from a list of paths. Note that each read file may contain
     * any number of certificates.
     *
     * @param paths List of filesystem paths to trusted certificate files.
     * @return New {@link TruststorePEM}.
     */
    public static TruststorePEM read(final List<Path> paths) throws IOException {
        final var trustedCertificates = new ArrayList<byte[]>(paths.size());
        for (final var path : paths) {
            trustedCertificates.add(Files.readAllBytes(path));
        }
        return new TruststorePEM(trustedCertificates);
    }

    /**
     * Creates new {@link TruststorePEM} by reading PEM encoded x.509
     * certificates from an array of paths. Note that each read file may
     * contain any number of certificates.
     *
     * @param paths List of filesystem paths to trusted certificate files.
     * @return New {@link TruststorePEM}.
     */
    public static TruststorePEM read(final Path... paths) throws IOException {
        return read(Arrays.asList(paths));
    }

    /**
     * @return Contents of PEM files, each containing one or more PEM encoded
     * x.509 certificates.
     */
    public List<byte[]> trustedCertificates() {
        return trustedCertificates;
    }
}
