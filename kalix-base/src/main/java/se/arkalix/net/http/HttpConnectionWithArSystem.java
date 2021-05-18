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
     * Gets the Arrowhead system identity of the system at the remote end of
     * this connection.
     *
     * @return Remote system identity.
     * @throws SecurityDisabled If this connection is not secure.
     */
    SystemIdentity remoteIdentity();

    /**
     * Gets a description of the Arrowhead system at the remote end of this
     * connection.
     *
     * @return Remote system record.
     */
    default SystemRecordWithIdentity remoteSystem() {
        return isSecure()
            ? SystemRecordWithIdentity.from(remoteIdentity(), remoteSocketAddress())
            : SystemRecordWithIdentity.from("<" + remoteSocketAddress() + ">", remoteSocketAddress(), null);
    }

    @Override
    default Certificate[] localCertificateChain() {
        return localIdentity().chain();
    }

    /**
     * Gets the Arrowhead system identity of the system at the local end of
     * this connection.
     *
     * @return Local system identity.
     * @throws SecurityDisabled If this connection is not secure.
     */
    default OwnedIdentity localIdentity() {
        return localSystem().identity();
    }

    /**
     * Gets the Arrowhead system at the local end of this connection.
     *
     * @return Local Arrowhead system.
     */
    ArSystem localSystem();
}
