package eu.arrowhead.kalix.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents a PEM encoded x.509 certificate chain the PKCS#8 private key
 * of the last certificate in that chain.
 * <p>
 * Note that PKCS#1 is not supported. PKCS#8 entries of PEM files are
 * distinguished by their using the {@code -----BEGIN PRIVATE KEY-----}
 * delimiter instead of the {@code -----BEGIN RSA PRIVATE KEY-----} that is
 * used by PKCS#1. If your private key is in PKCS#1, it must first be
 * converted to PKCS#8.
 */
public class KeyStorePEM implements KeyStore {
    private final byte[] certificateChain;
    private final byte[] privateKey;

    /**
     * Creates new {@link KeyStorePEM} from given byte arrays.
     *
     * @param certificateChain PEM encoded x.509 certificates.
     * @param privateKey       PEM encoded PKCS#8 private key.
     */
    public KeyStorePEM(final byte[] certificateChain, byte[] privateKey) {
        this.certificateChain = Objects.requireNonNull(certificateChain);
        this.privateKey = Objects.requireNonNull(privateKey);
    }

    /**
     * Creates new {@link KeyStorePEM} from contents of identified files.
     *
     * @param pathToCertificateChain Path to file containing PEM encoded x.509
     *                               certificate chain, typically having one of
     *                               the {@code .pem} or {@code .crt} file
     *                               extensions.
     * @param pathToPrivateKey       Path to file containing PEM encoded PKCS#8
     *                               private key, typically having one of the
     *                               {@code .pem} or {@code .key} file
     *                               extensions.
     */
    public static KeyStorePEM read(final Path pathToCertificateChain, final Path pathToPrivateKey)
        throws IOException {
        return new KeyStorePEM(
            Files.readAllBytes(pathToCertificateChain),
            Files.readAllBytes(pathToPrivateKey)
        );
    }

    /**
     * @return PEM encoded x.509 certificate chain.
     */
    public byte[] getCertificateChain() {
        return certificateChain;
    }

    /**
     * @return PEM encoded PKCS#8 private key.
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }
}