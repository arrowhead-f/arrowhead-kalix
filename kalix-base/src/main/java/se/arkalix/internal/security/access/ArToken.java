package se.arkalix.internal.security.access;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import se.arkalix.descriptor.InterfaceDescriptor;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Arrowhead JSON Web Token (JWT).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7519">RFC 7519</a>
 */
public final class ArToken {
    private static final long CLOCK_SKEW_TOLERANCE_IN_MS = 60_000;

    private final String cid;
    private final InterfaceDescriptor iid;
    private final String sid;

    private ArToken(final String cid, final InterfaceDescriptor iid, final String sid) {
        this.cid = cid;
        this.iid = iid;
        this.sid = sid;
    }

    public String cid() {
        return cid;
    }

    public InterfaceDescriptor iid() {
        return iid;
    }

    public String sid() {
        return sid;
    }

    public static ArToken read(final byte[] token, final PrivateKey receiverKey, final PublicKey senderKey)
        throws ArTokenException
    {
        // Decrypt, verify and collect JWT claims.
        final var plaintext = decrypt(token, receiverKey);
        final var payload = verifySignatureAndGetPayload(plaintext, senderKey);
        final var claims = verifyAndGetClaims(payload);

        // Collect consumer identifier.
        final var cid = claims.getClaimValueAsString("cid");
        if (cid == null) {
            throw new ArTokenException("Expected \"cid\" claim");
        }

        // Collect interface identifier.
        final var iid = claims.getClaimValueAsString("iid");
        if (iid == null) {
            throw new ArTokenException("Expected \"iid\" claim");
        }
        InterfaceDescriptor iid0;
        try {
            iid0 = InterfaceDescriptor.valueOf(iid);
        }
        catch (final IllegalArgumentException exception) {
            throw new ArTokenException("Malformed \"iid\"; expected " +
                "<protocol>-<security>-<encoding>, got \"" + iid + "\"");
        }

        // Collect service identifier.
        final var sid = claims.getClaimValueAsString("sid");
        if (sid == null) {
            throw new ArTokenException("Expected \"sid\" claim");
        }

        return new ArToken(cid, iid0, sid);
    }

    private static String decrypt(final byte[] message, final PrivateKey receiverKey) throws ArTokenException {
        final var jwe = new JsonWebEncryption();
        try {
            jwe.setCompactSerialization(new String(message, StandardCharsets.UTF_8));
        }
        catch (final JoseException exception) {
            throw new ArTokenException("Malformed JWE", exception);
        }
        jwe.setKey(receiverKey);
        try {
            return jwe.getPlaintextString();
        }
        catch (final JoseException exception) {
            throw new ArTokenException("Could not decrypt token", exception);
        }
    }

    public static String verifySignatureAndGetPayload(final String message, final PublicKey senderKey) throws ArTokenException {
        final var jws = new JsonWebSignature();
        try {
            jws.setCompactSerialization(message);
        }
        catch (final JoseException exception) {
            throw new ArTokenException("Malformed JWS", exception);
        }
        jws.setKey(senderKey);

        JoseException cause = null;
        boolean isValid;
        try {
            isValid = jws.verifySignature();
        }
        catch (final JoseException exception) {
            cause = exception;
            isValid = false;
        }
        if (!isValid) {
            throw new ArTokenException("Token signature verification failed", cause);
        }

        try {
            return jws.getPayload();
        }
        catch (final JoseException exception) {
            throw new ArTokenException("Malformed JWS payload", exception);
        }
    }

    private static JwtClaims verifyAndGetClaims(final String payload) throws ArTokenException {
        try {
            final var claims = JwtClaims.parse(payload);

            final var now = System.currentTimeMillis();

            final var exp = claims.getExpirationTime();
            if (exp != null && exp.getValueInMillis() < now + CLOCK_SKEW_TOLERANCE_IN_MS) {
                throw new ArTokenException("JWT expired");
            }

            final var iat = claims.getIssuedAt();
            if (iat != null && iat.getValueInMillis() > now - CLOCK_SKEW_TOLERANCE_IN_MS) {
                throw new ArTokenException("JWT not yet issued");
            }

            return claims;
        }
        catch (final InvalidJwtException exception) {
            throw new ArTokenException("Malformed JWT claims", exception);
        }
        catch (final MalformedClaimException exception) {
            throw new ArTokenException("Malformed JWT claim", exception);
        }
    }
}
