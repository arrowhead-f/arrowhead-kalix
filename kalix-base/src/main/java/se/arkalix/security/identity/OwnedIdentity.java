package se.arkalix.security.identity;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Objects;

/**
 * Holds the Arrowhead certificate chain and private key required to manage an
 * <i>owned</i> system or operator identity.
 * <p>
 * Instances of this class are guaranteed to only hold x.509 certificates
 * complying to the Arrowhead certificate {@link se.arkalix.security.identity
 * naming conventions}.
 *
 * @see se.arkalix.security.identity Arrowhead Identity Management
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 */
@SuppressWarnings("unused")
public class OwnedIdentity extends SystemIdentity {
    private final PrivateKey privateKey;

    /**
     * Creates new owned identity from given chain of x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity of a system, the
     * certificate at index 1 the identity of a cloud, while all subsequent
     * certificates constitute their chain of issuers. The certificates,
     * including the first two, must be in order in the sense that the
     * certificate at index {@code n} must be issued by the certificate at
     * index {@code n + 1}.
     *
     * @param chain      x.509 certificate chain belonging to an Arrowhead
     *                   system.
     * @param privateKey Private key associated with the system certificate.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0}, if
     *                                  {@code chain} contains any other type
     *                                  of certificate than
     *                                  {@link X509Certificate}, or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final Certificate[] chain, final PrivateKey privateKey) {
        super(chain);
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        verify();
    }

    /**
     * Creates new system identity from given chain of x.509 certificates.
     * <p>
     * The certificate at index 0 must represent the identity of a system, the
     * certificate at index 1 the identity of a cloud, while all subsequent
     * certificates constitute their chain of issuers. The certificates,
     * including the first two, must be in order in the sense that the
     * certificate at index {@code n} must be issued by the certificate at
     * index {@code n + 1}.
     *
     * @param chain      x.509 certificate chain belonging to an Arrowhead
     *                   system.
     * @param privateKey Private key associated with the system certificate.
     * @throws NullPointerException     If {@code chain} is {@code null}.
     * @throws IllegalArgumentException If {@code chain.length == 0} or if the
     *                                  subject common name of the certificate
     *                                  at index 0 is not a valid Arrowhead
     *                                  system certificate name.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final X509Certificate[] chain, final PrivateKey privateKey) {
        super(chain);
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        verify();
    }

    /**
     * Promotes given {@code identity} to an {@link OwnedIdentity}.
     * <p>
     * The promotion will succeed only if the certificate of the given
     * {@code identity} contains an Arrowhead-compliant subject common name.
     *
     * @param identity   Identity to promote.
     * @param privateKey Private key associated with system certificate.
     * @throws IllegalArgumentException If given {@code identity} does not
     *                                  satisfy the Arrowhead naming
     *                                  requirements.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final SystemIdentity identity, final PrivateKey privateKey) {
        super(identity);
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        verify();
    }

    /**
     * Promotes given {@code identity} to an {@link OwnedIdentity}.
     * <p>
     * The promotion will succeed only if the certificate of the given
     * {@code identity} contains an Arrowhead-compliant subject common name.
     *
     * @param identity   Identity to promote.
     * @param privateKey Private key associated with system certificate.
     * @throws IllegalArgumentException If given {@code identity} does not
     *                                  satisfy the Arrowhead naming
     *                                  requirements.
     * @see SystemIdentity Class description for details on valid names.
     */
    public OwnedIdentity(final TrustedIdentity identity, final PrivateKey privateKey) {
        super(identity);
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        verify();
    }

    private void verify() {
        final var cloud = cloud();

        final String cln;
        try {
            cln = cloud.commonName();
        }
        catch (final IllegalStateException exception) {
            throw new IllegalArgumentException("Cloud certificate does not " +
                "contain a subject common name", exception);
        }

        final var syn = commonName();
        final var off = name().length() + 1;
        final var len = Math.min(syn.length() - off, cln.length());
        if (!syn.regionMatches(off, cln, 0, len)) {
            throw new IllegalArgumentException("Cloud certificate common name " +
                "expected to be \"" + syn.substring(off) + "\"; found \"" +
                cln + "\"");
        }
    }

    /**
     * @return Private key associated key store system.
     */
    public PrivateKey privateKey() {
        return privateKey;
    }

    /**
     * Helper class useful for creating {@link OwnedIdentity} instances.
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
         * As of Java 11, only the
         * <a href="https://tools.ietf.org/html/rfc7292">PKCS#12</a> key store
         * format is mandatory to support for Java implementations. Your JVM
         * version may support additional formats.
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
         * As of Java 11, only the
         * <a href="https://tools.ietf.org/html/rfc7292">PKCS#12</a> key store
         * format is mandatory to support for Java implementations. Your JVM
         * version may support additional formats.
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
         * Sets path to file containing JVM-compatible key store.
         * <p>
         * As of Java 11, only the PKCS#12 key store format is mandatory to
         * support for Java implementations. Your JVM version may support
         * additional formats.
         * <p>
         * Note that it is an error to provide both a {@code keyStorePath} and
         * a {@code keyStore} via {@link #keyStore(KeyStore)}. However, at
         * least one of them must be provided.
         *
         * @param keyStorePath Path to key store to use.
         * @return This loader.
         * @see <a href="https://tools.ietf.org/html/rfc7292">RFC 7292</a>
         */
        public Loader keyStorePath(final String keyStorePath) {
            return keyStorePath(Path.of(keyStorePath));
        }

        /**
         * @param keyStorePassword Password of provided key store, if required.
         * @return This loader.
         */
        public Loader keyStorePassword(final char[] keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        /**
         * @param keyAlias Alias of certificate with associated private key in
         *                 provided key store. Can be omitted if the provided
         *                 key store only contains one certificate with a
         *                 private key.
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
         * new {@link OwnedIdentity}.
         *
         * @return Loaded identity.
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
        public OwnedIdentity load() throws GeneralSecurityException, IOException {
            if (keyStore == null && keyStorePath == null) {
                throw new NullPointerException("keyStore or keyStorePath");
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
                        "store is associated with a private key " +
                        (keyPassword != null
                            ? "accessible with the provided password"
                            : "without a password"));
                }
                if (keyAliases.length() > 0) {
                    throw new KeyStoreException("The following aliases are " +
                        "associated with private keys in the provided key" +
                        "store " + keyAliases + keyAlias + "; specify which " +
                        "of them to use");
                }
            }

            final var protection = keyPassword != null ? new KeyStore.PasswordProtection(keyPassword) : null;
            final var entry = keyStore.getEntry(keyAlias, protection);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                throw new KeyStoreException("Alias \"" + keyAlias + "\" is " +
                    "not associated with a private key; cannot load key store");
            }
            final var privateKeyEntry = (KeyStore.PrivateKeyEntry) entry;

            return new OwnedIdentity(privateKeyEntry.getCertificateChain(), privateKeyEntry.getPrivateKey());
        }
    }
}
