package se.arkalix.net.http;

import se.arkalix.ArSystem;
import se.arkalix.SystemRecordWithIdentity;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.SystemIdentity;

import java.security.cert.Certificate;

/**
 * An {@link HttpConnection} established between a local and a remote Arrowhead
 * system.
 */
public interface HttpConnectionWithArSystem extends HttpConnection {
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

    /**
     * Gets a description of the Arrowhead system at the remote end of this
     * connection.
     *
     * @return Provider system identity description.
     */
    default SystemRecordWithIdentity remoteSystem() {
        return isSecure()
            ? SystemRecordWithIdentity.from(remoteIdentity(), remoteSocketAddress())
            : SystemRecordWithIdentity.from("<" + remoteSocketAddress() + ">", remoteSocketAddress());
    }

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
