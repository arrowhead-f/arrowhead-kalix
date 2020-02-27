package eu.arrowhead.kalix.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents the contents of some PKCS#12 keystore and its password.
 * <p>
 * The PKCS#12 keystore must contain at least one x.509 certificate and its
 * corresponding private key.
 *
 * @see <a href="https://tools.ietf.org/html/rfc5280">RFC 5280</a>
 * @see <a href="https://tools.ietf.org/html/rfc7292">RFC 7292</a>
 */
public class KeystorePKCS12 implements Keystore {
    private final byte[] contents;
    private final String password;
    private final String alias;

    /**
     * Creates new {@link KeystorePKCS12} from PKCS#12 keystore contents, a
     * password and an optional alias. If the keystore contains more than one
     * certificate with a private key, the alias of that certificate should be
     * provided.
     *
     * @param contents Raw contents of PKCS#12 keystore.
     * @param password Password of PKCS#12 keystore.
     * @param alias    Alias of primary/preferred certificate, or {@code null}.
     */
    public KeystorePKCS12(final byte[] contents, final String password, final String alias) {
        this.contents = Objects.requireNonNull(contents);
        this.password = Objects.requireNonNull(password);
        this.alias = alias;
    }

    /**
     * Creates new {@link KeystorePKCS12} from the path to a PKCS#12 keystore,
     * a password and an optional alias. If the PKCS#12 keystore contains more
     * than one certificate with a private key, the alias is used to pinpoint
     * that certificate.
     *
     * @param path     Filesystem path to a PKCS#12 keystore, which typically has
     *                 the {@code .p12} file extension.
     * @param password Password of PKCS#12 keystore.
     * @param alias    Alias of primary/preferred certificate, or {@code null}.
     */
    public static KeystorePKCS12 read(final Path path, final String password, final String alias) throws IOException {
        return new KeystorePKCS12(Files.readAllBytes(path), password, alias);
    }

    /**
     * @return PKCS#12 keystore as an array of bytes.
     */
    public byte[] contents() {
        return contents;
    }

    /**
     * @return PKCS#12 keystore password.
     */
    public String password() {
        return password;
    }

    /**
     * @return Alias of primary/preferred PKCS#12 keystore certificate, or
     * {@code null} no such has been designated.
     */
    public String alias() {
        return alias;
    }
}
