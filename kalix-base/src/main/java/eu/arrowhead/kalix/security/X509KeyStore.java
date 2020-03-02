package eu.arrowhead.kalix.security;

import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * Holds the x.509 certificates and private key required to manage an
 * <i>owned</i> identity.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
public class X509KeyStore {
    private final X509Certificate[] certificateChain;
    private final X509Certificate certificate;
    private final PrivateKey privateKey;

    /**
     * Creates new x.509 key store from provided certificates and private key.
     *
     * @param chain       Certificate chain establishing credibility of
     *                    {@code certificate}.
     * @param certificate Owned certificate.
     * @param key         Private key associated with {@code certificate}.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509KeyStore(final X509Certificate[] chain, final X509Certificate certificate, final PrivateKey key) {
        this.certificateChain = chain;
        this.certificate = certificate;
        this.privateKey = key;
    }

    /**
     * @return Clone of chain of certificates, each being signed by the next
     * that establish the credibility of the certificate returned by
     * {@link #certificate()}, which will be located at index 0 of the returned
     * array.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509Certificate[] certificateChain() {
        return certificateChain.clone();
    }

    /**
     * @return Owned certificate.
     * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
     */
    public X509Certificate certificate() {
        return certificate;
    }

    /**
     * @return Private key associated with certificate returned by
     * {@link #certificate()}.
     */
    public PrivateKey privateKey() {
        return privateKey;
    }

    /**
     * @return Public key present in certificate returned by
     * {@link #certificate()}.
     */
    public PublicKey publicKey() {
        return certificate.getPublicKey();
    }

    /**
     * Helper class useful for creating {@link X509KeyStore} instances.
     */
    public static final class Loader {
        private KeyStore keyStore;
        private Path keyStorePath;
        private char[] keyStorePassword;
        private String keyAlias;
        private char[] keyPassword;

        /**
         * Sets JVM-compatible {@link KeyStore} instance to use.
         * <p>
         * As of Java 11, only the PKCS#12 key store format is mandatory to
         * support for Java implementations.
         * <p>
         * Note that it is an error to provide both a {@code keyStore} and a
         * {@code keyStorePath} via {@link #keyStorePath(Path)}. However, at
         * least one of them must be provided.
         *
         * @param keyStore Key store to use.
         * @return This loader.
         * @see <a href="https://tools.ietf.org/html/rfc7292">RFC 7292</a>
         */
        public Loader keyStore(final KeyStore keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        /**
         * Sets path to file containing JVM-compatible key store.
         * <p>
         * As of Java 11, only the PKCS#12 key store format is mandatory to
         * support for Java implementations.
         * <p>
         * Note that it is an error to provide both a {@code keyStorePath} and
         * a {@code keyStore} via {@link #keyStore(KeyStore)}. However, at
         * least one of them must be provided.
         *
         * @param keyStorePath Path to key store to use.
         * @return This loader.
         * @see <a href="https://tools.ietf.org/html/rfc7292">RFC 7292</a>
         */
        public Loader keyStorePath(final Path keyStorePath) {
            this.keyStorePath = keyStorePath;
            return this;
        }

        /**
         * @param keyStorePassword Password of used key store, if required.
         * @return This loader.
         */
        public Loader keyStorePassword(final char[] keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        /**
         * @param keyAlias Alias of certificate with associated private key in
         *                 provided key store. Can be omitted if the provided
         *                 key store only contains one such certificate.
         * @return This loader.
         */
        public Loader keyAlias(final String keyAlias) {
            this.keyAlias = keyAlias;
            return this;
        }

        /**
         * @param keyPassword Password of private key associated with
         *                    designated certificate in key store, if required.
         * @return This loader.
         */
        public Loader keyPassword(final char[] keyPassword) {
            this.keyPassword = keyPassword;
            return this;
        }

        /**
         * Uses provided details to load key store and extract a certificate
         * chain, certificate and private key, and then uses these to create a
         * new {@link X509KeyStore} instance.
         *
         * @return Loaded x.509 key store instance.
         * @throws GeneralSecurityException If the key store does not contain
         *                                  the necessary certificates and
         *                                  private key, or it those cannot be
         *                                  accessed due to bad passwords
         *                                  and/or a bad alias being specified,
         *                                  or the key store contains data or
         *                                  details that cannot be interpreted
         *                                  or supported properly.
         * @throws IOException              If the key store at the specified
         *                                  {@code keyStorePath} could not be
         *                                  read.
         * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
         */
        public X509KeyStore load() throws GeneralSecurityException, IOException {
            if (keyStore == null && keyStorePath == null) {
                throw new NullPointerException("Expected keyStore or keyStorePath");
            }
            if (keyStore != null && keyStorePath != null) {
                throw new IllegalStateException("Provided both keyStore and keyStorePath");
            }

            if (keyStore == null) {
                final var keyStoreFile = keyStorePath.toFile();
                keyStore = keyStorePassword != null
                    ? KeyStore.getInstance(keyStoreFile, keyStorePassword)
                    : KeyStore.getInstance(keyStoreFile, (KeyStore.LoadStoreParameter) null);
            }

            if (keyAlias == null) {
                final var keyAliases = new StringBuilder(0);
                for (final var alias : Collections.list(keyStore.aliases())) {
                    if (keyStore.isKeyEntry(alias)) {
                        if (keyAlias == null) {
                            keyAlias = alias;
                        }
                        else {
                            keyAliases.append(alias).append(", ");
                        }
                    }
                }
                if (keyAlias == null) {
                    throw new KeyStoreException("No alias in provided key " +
                        "store is associated with a private key");
                }
                if (keyAliases.length() > 0) {
                    throw new KeyStoreException("The following aliases are " +
                        "associated with private keys in provided key store " +
                        keyAliases + keyAlias + "; specify which of them to " +
                        "use");
                }
            }

            final var protection = keyPassword != null
                ? new KeyStore.PasswordProtection(keyPassword)
                : null;
            final var entry = keyStore.getEntry(keyAlias, protection);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new KeyStoreException("Alias " + keyAlias + " is not " +
                    "associated with a private key");
            }
            final var privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;

            final var chain = privateKeyEntry.getCertificateChain();
            final var x509chain = new X509Certificate[chain.length];
            for (var i = 0; i < chain.length; ++i) {
                final var certificate = chain[i];
                if (!(certificate instanceof X509Certificate)) {
                    throw certificateNotPermitted(certificate);
                }
                x509chain[i] = (X509Certificate) chain[i];
            }

            final var certificate = privateKeyEntry.getCertificate();
            if (!(certificate instanceof X509Certificate)) {
                throw certificateNotPermitted(certificate);
            }
            final var x509Certificate = (X509Certificate) certificate;

            final var privateKey = privateKeyEntry.getPrivateKey();

            return new X509KeyStore(x509chain, x509Certificate, privateKey);
        }

        private KeyStoreException certificateNotPermitted(final Certificate certificate) {
            return new KeyStoreException("Only x.509 certificates are " +
                "permitted in provided key stores; the following certificate" +
                "does not comply with that specification: " + certificate);
        }
    }
}
