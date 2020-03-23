package se.arkalix.security.identity;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;

/**
 * An Arrowhead public key.
 * <p>
 * This class exists primarily for adding various convenience methods to the
 * Java 1.1 {@link PublicKey} class.
 */
public class ArPublicKey {
    private final PublicKey publicKey;

    private byte[] der = null;
    private String base64 = null;

    /**
     * Creates new Arrowhead public key from given Java 1.1 public key.
     *
     * @param publicKey Public key to wrap.
     * @return New Arrowhead public key.
     * @throws IllegalArgumentException If {@code publicKey} is not encoded
     *                                  with an x.509 format.
     */
    public static ArPublicKey of(final PublicKey publicKey) {
        if (!Objects.equals(publicKey.getFormat(), "X.509")) {
            throw new IllegalArgumentException("Not an x.509 public key");
        }
        return new ArPublicKey(publicKey);
    }

    private ArPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return Reference to wrapped Java 1.1 public key.
     */
    public PublicKey unwrap() {
        return publicKey;
    }

    /**
     * @return Public key, encoded with DER.
     * @see <a href="https://www.itu.int/rec/T-REC-X.690-201508-I/en">X.690: Information technology - ASN.1 encoding rules: Specification of Basic Encoding Rules (BER), Canonical Encoding Rules (CER) and Distinguished Encoding Rules (DER)</a>
     */
    public byte[] toDer() {
        if (der == null) {
            der = publicKey.getEncoded();
        }
        return der;
    }

    /**
     * @return Public key, first encoded with DER and then with Base64.
     * @see <a href="https://tools.ietf.org/html/rfc4648#section-4">RFC 4648, Section 4</a>
     * @see <a href="https://www.itu.int/rec/T-REC-X.690-201508-I/en">X.690: Information technology - ASN.1 encoding rules: Specification of Basic Encoding Rules (BER), Canonical Encoding Rules (CER) and Distinguished Encoding Rules (DER)</a>
     */
    public String toBase64() {
        if (base64 == null) {
            base64 = Base64.getEncoder().encodeToString(toDer());
        }
        return base64;
    }
}
