package se.arkalix.security.identity;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Holds certificates associated with <i>trusted</i> Arrowhead systems,
 * operators, clouds, companies and other authorities.
 * <p>
 * Instances of this class are guaranteed to only hold x.509 certificates
 * complying to the Arrowhead certificate {@link ArSystemCertificateChain naming
 * conventions}.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class ArTrustStore {
    private final X509Certificate[] certificates;

    /**
     * Creates new x.509 trust store from given array of certificates.
     *
     * @param certificates Trusted certificates.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public ArTrustStore(final X509Certificate... certificates) {
        this.certificates = certificates.clone();
    }

    /**
     * Creates new x.509 trust store by collecting all certificates from given
     * initialized {@link KeyStore}.
     *
     * @param keyStore Key store containing trusted certificates.
     * @return New x.509 trust store.
     * @throws KeyStoreException If {@code keyStore} has not been initialized.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public static ArTrustStore from(final KeyStore keyStore) throws KeyStoreException {
        final var certificates = new ArrayList<X509Certificate>();
        for (final var alias : Collections.list(keyStore.aliases())) {
            final var certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate)) {
                throw new KeyStoreException("Only x.509 certificates " +
                    "are permitted in ArTrustStore instances; the " +
                    "following certificate is of some other type: " +
                    certificate);
            }
            certificates.add((X509Certificate) certificate);
        }
        return new ArTrustStore(certificates.toArray(new X509Certificate[0]));
    }

    /**
     * Reads JVM-compatible key store from specified path and collects all
     * contained certificates into a created {@link ArTrustStore}.
     * <p>
     * As of Java 11, only the PKCS#12 key store format is mandatory to
     * support for Java implementations.
     *
     * @param path     Filesystem path to key store to load.
     * @param password Key store password, or {@code null} if not required.
     * @return New x.509 trust store.
     * @throws GeneralSecurityException If the key store contains data or
     *                                  details that cannot be interpreted
     *                                  or supported properly.
     * @throws IOException              If the key store at the specified
     *                                  {@code path} could not be read.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     * @see <a href="https://tools.ietf.org/html/rfc7292">RFC 7292</a>
     */
    public static ArTrustStore read(final Path path, final char[] password)
        throws GeneralSecurityException, IOException
    {
        final var file = path.toFile();
        final var keyStore = password != null
            ? KeyStore.getInstance(file, password)
            : KeyStore.getInstance(file, (KeyStore.LoadStoreParameter) null);
        return from(keyStore);
    }

    /**
     * @return Clone of array of trusted x.509 certificates.
     */
    public X509Certificate[] certificates() {
        return certificates.clone();
    }
}