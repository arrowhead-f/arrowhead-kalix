package se.arkalix.security;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Holds x.509 certificates associated with <i>trusted</i> identities.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class X509TrustStore {
    private final X509Certificate[] certificates;

    /**
     * Creates new x.509 trust store from given array of certificates.
     *
     * @param certificates Trusted certificates.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509TrustStore(final X509Certificate... certificates) {
        this.certificates = certificates;
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
    public static X509TrustStore from(final KeyStore keyStore) throws KeyStoreException {
        final var certificates = new ArrayList<X509Certificate>();
        for (final var alias : Collections.list(keyStore.aliases())) {
            final var certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate)) {
                throw new KeyStoreException("Only x.509 certificates are " +
                    "permitted in X509TrustStore instances; the following " +
                    "certificate does not comply with that specification: " +
                    certificate);
            }
            certificates.add((X509Certificate) certificate);
        }
        return new X509TrustStore(certificates.toArray(new X509Certificate[0]));
    }

    /**
     * Reads JVM-compatible key store from specified path and collects all
     * contained certificates into a created {@link X509TrustStore}.
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
    public static X509TrustStore read(final Path path, final char[] password)
        throws GeneralSecurityException, IOException
    {
        final var file = path.toFile();
        final var keyStore = password != null
            ? KeyStore.getInstance(file, password)
            : KeyStore.getInstance(file, (KeyStore.LoadStoreParameter) null);
        return from(keyStore);
    }

    /**
     * Loads default {@link javax.net.ssl.X509TrustManager X509TrustManager}
     * from the system and uses its list of accepted issuers to initialize a
     * new trust store.
     *
     * @return New x.509 trust store.
     * @throws GeneralSecurityException If loading default trust manager fails.
     */
    public static X509TrustStore systemDefault() throws GeneralSecurityException {
        final var factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);

        for (final var trustManager : factory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                return new X509TrustStore(((X509TrustManager) trustManager).getAcceptedIssuers());
            }
        }
        throw new IllegalStateException("No system default x.509 trust manager has been configured");
    }

    /**
     * @return Clone of trusted x.509 certificates.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509Certificate[] certificates() {
        return certificates.clone();
    }
}
