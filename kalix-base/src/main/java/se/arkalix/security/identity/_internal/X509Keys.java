package se.arkalix.security.identity._internal;

import se.arkalix.security.identity.UnsupportedKeyAlgorithm;
import se.arkalix.util.annotation.Internal;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Internal
public class X509Keys {
    private X509Keys() {}

    /**
     * Takes Base64 and DER encoded public key, formatted as specified by RFC
     * 5280, Section 4.1.2.7, and tries to convert it into a {@link PublicKey}.
     *
     * @param publicKeyBase64 Base64-encoded public key to convert.
     * @return Converted public key.
     * @throws NullPointerException    If {@code publicKeyBase64} is {@code
     *                                 null}.
     * @throws UnsupportedKeyAlgorithm If the public key algorithm could not be
     *                                 identified.
     */
    public static PublicKey parsePublicKey(final String publicKeyBase64) {
        Objects.requireNonNull(publicKeyBase64, "Expected publicKeyBase64");
        final var publicKeyDer = Base64.getDecoder().decode(publicKeyBase64);
        final var algorithm = X509Keys.identifyPublicKeyAlgorithm(publicKeyDer);
        if (algorithm.isEmpty()) {
            throw new UnsupportedKeyAlgorithm("Provided public key deems to " +
                "be using an unsupported key algorithm");
        }
        try {
            final var keySpec = new X509EncodedKeySpec(publicKeyDer);
            final var keyFactory = KeyFactory.getInstance(algorithm.get());
            return keyFactory.generatePublic(keySpec);
        }
        catch (final NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new RuntimeException(exception); // This should never happen.
        }
    }

    private static final byte[] OID_EC = new byte[]{
        (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0xCE, (byte) 0x3D,
        (byte) 0x02, (byte) 0x01};

    private static final byte[] OID_RSA = new byte[]{
        (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0x86, (byte) 0xF7,
        (byte) 0x0D, (byte) 0x01, (byte) 0x01, (byte) 0x01};

    private static final byte[] OID_DSA = new byte[]{
        (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0xCE, (byte) 0x38,
        (byte) 0x04, (byte) 0x01};

    private static final byte[] OID_DH = new byte[]{
        (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0xCE, (byte) 0x3E,
        (byte) 0x02, (byte) 0x01};

    private static final int TAG_OID = 0x06;
    private static final int TAG_SEQUENCE = 0x30;

    /**
     * Takes a DER-encoded public key, formatted as specified by RFC 5280,
     * Section 4.1.2.7, resolves its AlgorithmIdentifier OID and maps it to one
     * of the following {@link java.security.KeyFactory} algorithm names:
     * <ul>
     *     <li>"EC"</li>
     *     <li>"DSA"</li>
     *     <li>"DH"</li>
     *     <li>"RSA"</li>
     * </ul>
     *
     * @param publicKeyDer DER-encoded public key to identify the algorithm of.
     * @return {@link java.security.KeyFactory} algorithm name, if any such
     * could be identified.
     * @see <a href="https://tools.ietf.org/html/rfc3279">RFC 3279</a>
     * @see <a href="https://tools.ietf.org/html/rfc5280#section-4.1.2.7">RFC 5280, Section 4.1.2.7</a>
     */
    static Optional<String> identifyPublicKeyAlgorithm(final byte[] publicKeyDer) {
        String name = null;

        done:
        {
            int offset = 0;

            if (publicKeyDer[offset++] != TAG_SEQUENCE) {
                break done;
            }
            offset += lengthSize(publicKeyDer[offset]);

            if (publicKeyDer[offset++] != TAG_SEQUENCE) {
                break done;
            }
            offset += lengthSize(publicKeyDer[offset]);

            if (publicKeyDer[offset++] != TAG_OID) {
                break done;
            }

            switch (publicKeyDer[offset++]) {
            case 7:
                final var end = offset + 7;
                if (Arrays.equals(publicKeyDer, offset, end, OID_EC, 0, 7)) {
                    name = "EC";
                    break;
                }
                if (Arrays.equals(publicKeyDer, offset, end, OID_DSA, 0, 7)) {
                    name = "DSA";
                    break;
                }
                if (Arrays.equals(publicKeyDer, offset, end, OID_DH, 0, 7)) {
                    name = "DH";
                }
                break;

            case 9:
                if (Arrays.equals(publicKeyDer, offset, offset + 9, OID_RSA, 0, 9)) {
                    name = "RSA";
                }
                break;

            default:
            }
        }

        return Optional.ofNullable(name);
    }

    private static int lengthSize(final int length) {
        return length >= 0
            ? 1
            : 1 + (length & 0x7F);
    }
}
