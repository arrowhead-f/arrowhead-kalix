package eu.arrowhead.kalix.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents the contents of some PKCS#12 truststore and its password.
 * <p>
 * The PKCS#12 truststore is expected to contain trusted x.509 certificates.
 */
public class TrustStorePKCS12 implements TrustStore {
    private final byte[] contents;
    private final String password;

    /**
     * Creates new {@link TrustStorePKCS12} from PKCS#12 truststore contents
     * and a password.
     *
     * @param contents Raw contents of PKCS#12 truststore.
     * @param password Password of PKCS#12 truststore.
     */
    public TrustStorePKCS12(final byte[] contents, final String password) {
        this.contents = Objects.requireNonNull(contents);
        this.password = Objects.requireNonNull(password);
    }

    /**
     * Creates new {@link TrustStorePKCS12} from the path to a PKCS#12
     * truststore and a password.
     *
     * @param path     Filesystem path to a PKCS#12 truststore, which typically has
     *                 the {@code .p12} file extension.
     * @param password Password of PKCS#12 truststore.
     */
    public static TrustStorePKCS12 read(final Path path, final String password) throws IOException {
        return new TrustStorePKCS12(Files.readAllBytes(path), password);
    }

    /**
     * @return PKCS#12 truststore as an array of bytes.
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     * @return PKCS#12 truststore password.
     */
    public String getPassword() {
        return password;
    }
}
