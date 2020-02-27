package eu.arrowhead.kalix.security;

import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.X509Certificate;

public class OwnedIdentity {
    private final X509Certificate[] certificateChain;
    private final X509Certificate certificate;
    private final PrivateKey privateKey;

    public OwnedIdentity(
        final X509Certificate[] certificateChain,
        final X509Certificate certificate,
        final PrivateKey privateKey
    ) {
        this.certificateChain = certificateChain;
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    public static OwnedIdentity readFromKeyStore(final Path path, final String password, final String alias)
        throws GeneralSecurityException, IOException {
        final var keyStore = KeyStore.getInstance(path.toFile(), password.toCharArray());
        if (!keyStore.containsAlias(alias)) {
            throw new KeyStoreException("Alias \"" + alias + "\" not in key store: " + path);
        }

        // TODO: Check casts. Tidy up.
        final var privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore
            .getEntry(alias, new KeyStore.PasswordProtection(password.toCharArray()));

        final var certificateChain = privateKeyEntry.getCertificateChain();
        final var x509certificateChain = new X509Certificate[certificateChain.length];
        for (var i = 0; i < certificateChain.length; ++i) {
            x509certificateChain[i] = (X509Certificate) certificateChain[i];
        }

        return new OwnedIdentity(
            x509certificateChain,
            (X509Certificate) privateKeyEntry.getCertificate(),
            privateKeyEntry.getPrivateKey());
    }

    public X509Certificate[] certificateChain() {
        return certificateChain;
    }

    public X509Certificate certificate() {
        return certificate;
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

}
