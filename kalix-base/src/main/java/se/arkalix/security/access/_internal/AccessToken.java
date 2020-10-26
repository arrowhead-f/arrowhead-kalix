package se.arkalix.security.access._internal;

import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import se.arkalix.descriptor.InterfaceDescriptor;
import se.arkalix.security.access.AccessTokenException;
import se.arkalix.util.annotation.Internal;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Arrowhead JSON Web Token (JWT).
 *
 * @see <a href="https://tools.ietf.org/html/rfc7519">RFC 7519</a>
 */
@Internal
public final class AccessToken {
    private static final long CLOCK_SKEW_TOLERANCE_IN_MS = 60_000;

    private final String cid;
    private final InterfaceDescriptor iid;
    private final String sid;

    private AccessToken(final String cid, final InterfaceDescriptor iid, final String sid) {
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

    public static AccessToken read(final String token, final PrivateKey receiverKey, final PublicKey senderKey)
        throws AccessTokenException
    {
        // Decrypt, verify and collect JWT claims.
        final var plaintext = decrypt(token, receiverKey);
        final var payload = verifySignatureAndGetPayload(plaintext, senderKey);
        final var claims = verifyAndGetClaims(payload);

        // Collect consumer identifier.
        final var cid = claims.getClaimValueAsString("cid");
        if (cid == null) {
            throw new AccessTokenException("Expected \"cid\" claim");
        }

        // Collect interface identifier.
        final var iid = claims.getClaimValueAsString("iid");
        if (iid == null) {
            throw new AccessTokenException("Expected \"iid\" claim");
        }
        InterfaceDescriptor iid0;
        try {
            iid0 = InterfaceDescriptor.valueOf(iid);
        }
        catch (final IllegalArgumentException exception) {
            throw new AccessTokenException("Malformed \"iid\"; expected " +
                "<protocol>-<security>-<encoding>, got \"" + iid + "\"");
        }

        // Collect service identifier.
        final var sid = claims.getClaimValueAsString("sid");
        if (sid == null) {
            throw new AccessTokenException("Expected \"sid\" claim");
        }

        return new AccessToken(cid, iid0, sid);
    }

    private static String decrypt(final String message, final PrivateKey receiverKey) throws AccessTokenException {
        final var jwe = new JsonWebEncryption();
        try {
            jwe.setCompactSerialization(message);
        }
        catch (final JoseException exception) {
            throw new AccessTokenException("Malformed JWE", exception);
        }
        jwe.setKey(receiverKey);
        try {
            return jwe.getPlaintextString();
        }
        catch (final JoseException exception) {
            throw new AccessTokenException("Could not decrypt token", exception);
        }
    }

    public static String verifySignatureAndGetPayload(
        final String message,
        final PublicKey senderKey) throws AccessTokenException
    {
        final var jws = new JsonWebSignature();
        try {
            jws.setCompactSerialization(message);
        }
        catch (final JoseException exception) {
            throw new AccessTokenException("Malformed JWS", exception);
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
            throw new AccessTokenException("Token signature verification failed", cause);
        }

        try {
            return jws.getPayload();
        }
        catch (final JoseException exception) {
            throw new AccessTokenException("Malformed JWS payload", exception);
        }
    }

    private static JwtClaims verifyAndGetClaims(final String payload) throws AccessTokenException {
        try {
            final var claims = JwtClaims.parse(payload);

            final var now = System.currentTimeMillis() + CLOCK_SKEW_TOLERANCE_IN_MS;

            final var exp = claims.getExpirationTime();
            if (exp != null && exp.getValueInMillis() > now) {
                throw new AccessTokenException("JWT expired");
            }

            final var iat = claims.getIssuedAt();
            if (iat != null && iat.getValueInMillis() > now) {
                throw new AccessTokenException("JWT not yet issued");
            }

            return claims;
        }
        catch (final InvalidJwtException exception) {
            throw new AccessTokenException("Malformed JWT claims", exception);
        }
        catch (final MalformedClaimException exception) {
            throw new AccessTokenException("Malformed JWT claim", exception);
        }
    }
}
