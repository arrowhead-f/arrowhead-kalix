package eu.arrowhead.kalix.security;

/**
 * Represents the x.509 certificates used to represent both an owned identity,
 * as well as a set of trusted identities.
 */
public class X509Profile {
    private final X509KeyStore keyStore;
    private final X509TrustStore trustStore;

    /**
     * Creates new x.509 profile from given key and trust stores.
     *
     * @param keyStore   Key store, representing an owned identity.
     * @param trustStore Trust store, representing a set of trusted identities.
     */
    public X509Profile(final X509KeyStore keyStore, final X509TrustStore trustStore) {
        this.keyStore = keyStore;
        this.trustStore = trustStore;
    }

    /**
     * @return Profile key store.
     */
    public X509KeyStore keyStore() {
        return keyStore;
    }

    /**
     * @return Profile trust store.
     */
    public X509TrustStore trustStore() {
        return trustStore;
    }
}
