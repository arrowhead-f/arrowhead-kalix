package se.arkalix.net.http.consumer;

import se.arkalix.ArSystem;
import se.arkalix.net.http.HttpIncomingResponse;
import se.arkalix.net.http.HttpOutgoingConnection;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.SystemIdentity;

import java.security.cert.Certificate;

/**
 * Represents an established HTTP connection used to consume a remote service.
 */
public interface HttpConsumerConnection extends HttpOutgoingConnection<HttpConsumerRequest, HttpIncomingResponse> {
    @Override
    default Certificate[] remoteCertificateChain() {
        return remoteIdentity().chain();
    }

    /**
     * Gets the Arrowhead system identity of the connected service provider.
     *
     * @return System identity of service provider.
     * @throws SecurityDisabled If this connection is not secure.
     */
    SystemIdentity remoteIdentity();

    @Override
    default Certificate[] localCertificateChain() {
        return localIdentity().chain();
    }

    /**
     * Gets the Arrowhead system identity of the system that established this
     * connection.
     *
     * @return System identity of service consumer.
     * @throws SecurityDisabled If this connection is not secure.
     */
    default OwnedIdentity localIdentity() {
        return localSystem().identity();
    }

    /**
     * Gets the Arrowhead system that established this connection.
     *
     * @return Arrowhead system.
     */
    ArSystem localSystem();
}
