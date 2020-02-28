package eu.arrowhead.kalix;

import eu.arrowhead.kalix.descriptor.ServiceDescriptor;
import eu.arrowhead.kalix.security.X509Certificates;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.concurrent.Scheduler;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;

public abstract class ArrowheadSystem<S> {
    private final String name;
    private final InetSocketAddress address;
    private final X509KeyStore keyStore;
    private final X509TrustStore trustStore;
    private final Scheduler scheduler;

    protected ArrowheadSystem(final Builder<?, ? extends ArrowheadSystem<?>> builder) {
        var name = builder.name;
        address = Objects.requireNonNullElseGet(builder.socketAddress, () -> new InetSocketAddress(0));
        keyStore = builder.keyStore;
        trustStore = builder.trustStore;
        scheduler = Objects.requireNonNullElseGet(builder.scheduler, Scheduler::create);

        if (builder.isInsecure) {
            if (name == null || name.length() == 0) {
                throw new IllegalStateException("Expected name; required in " +
                    "insecure mode");
            }
            this.name = name;

            if (keyStore != null || trustStore != null) {
                throw new IllegalStateException("Unexpected keyStore or " +
                    "trustStore; not used in insecure mode");
            }
        }
        else {
            if (keyStore == null || trustStore == null) {
                throw new IllegalStateException("Expected keyStore and " +
                    "trustStore; required in secure mode");
            }

            final var certificateName = X509Certificates.systemNameOf(keyStore.certificate());
            if (name != null && !Objects.equals(name, certificateName)) {
                throw new IllegalStateException("System name in provided " +
                    "keyStore certificate is \"" + certificateName + "\" " +
                    "while \"" + name + "\" was explicitly specified; " +
                    "either do not provide a name or provide the same name " +
                    "as is stated in the certificate");
            }
            this.name = certificateName;
        }
    }

    public String name() {
        return name;
    }

    public InetSocketAddress address() {
        return address;
    }

    public int port() {
        return address.getPort();
    }

    public X509KeyStore keyStore() {
        return keyStore;
    }

    public X509TrustStore trustStore() {
        return trustStore;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public abstract Set<ServiceDescriptor> providedServices();

    public abstract void provideService(final S service);

    public abstract void dismissService(final S service);

    public static abstract class Builder<B extends Builder<?, AS>, AS> {
        private String name;
        private InetSocketAddress socketAddress;
        private X509KeyStore keyStore;
        private X509TrustStore trustStore;
        private Scheduler scheduler;
        private boolean isInsecure = false;

        protected abstract B self();

        /**
         * Sets system name.
         * <p>
         * If running this Arrowhead system in secure mode, the name should
         * either be left unspecified or match the least significant part of
         * the Common Name (CN) in the system certificate. For example, if the
         * CN is "system1.cloud1.arrowhead.eu", then the name must be
         * "system1" or not be set at all. Note that the Arrowhead standard
         * demands that the common name is a dot-separated hierarchical name.
         * If not set, then the least significant part of the certificate
         * provided via {@link #keyStore(X509KeyStore)} is used as name.
         * <p>
         * If not running in secure mode, the name must be specified
         * explicitly. Avoid picking names that contain whitespace or anything
         * but alphanumeric ASCII characters and dashes.
         *
         * @param name Name of this system.
         * @return This builder.
         */
        public final B name(final String name) {
            this.name = name;
            return self();
        }

        /**
         * Sets the network interface and socket port number to be used by this
         * system when providing its services.
         * <p>
         * If a socket address and/or port has been set previously with any of
         * the other builder setters, only the address is updated by this
         * method.
         * <p>
         * If no socket address or port is specified, the wildcard network
         * interface will be used and a random port will be selected by the
         * system.
         *
         * @param address Internet address associated with the preferred
         *                network interface.
         * @return This builder.
         */
        public final B address(final InetAddress address) {
            if (socketAddress != null) {
                return socketAddress(address, socketAddress.getPort());
            }
            return socketAddress(new InetSocketAddress(address, 0));
        }

        /**
         * Sets the network interface and socket port number to be used by this
         * system when providing its services.
         * <p>
         * If no socket address or port is specified, the wildcard network
         * interface will be used and a random port will be selected by the
         * system.
         *
         * @param socketAddress Internet socket address associated with the
         *                      preferred network interface.
         * @return This builder.
         */
        public final B socketAddress(final InetSocketAddress socketAddress) {
            this.socketAddress = socketAddress;
            return self();
        }

        /**
         * Sets the network interface by socketAddress and socket port number to be
         * used by this system when providing its services.
         * <p>
         * If no socket address or port is specified, the wildcard network
         * interface will be used and a random port will be selected by the
         * system.
         *
         * @param socketAddress Internet socketAddress associated with the
         *                      preferred network interface.
         * @param port          Socket port number. If 0 is provided, the
         *                      system will choose a random port.
         * @return This builder.
         */
        public final B socketAddress(final InetAddress socketAddress, final int port) {
            return socketAddress(new InetSocketAddress(socketAddress, port));
        }

        /**
         * Sets the network interface by hostname and socket port number to be
         * used by this system when providing its services.
         * <p>
         * If no socket address or port is specified, the wildcard network
         * interface will be used and a random port will be selected by the
         * system.
         *
         * @param hostname DNS hostname associated with preferred network
         *                 interface.
         * @param port     Socket port number. If 0 is provided, the system
         *                 will choose a random port.
         * @return This builder.
         */
        public final B socketAddress(final String hostname, final int port) {
            return socketAddress(new InetSocketAddress(hostname, port));
        }

        /**
         * Sets the socket port number to be used by this system when providing
         * its services.
         * <p>
         * If a socket address has been set previously with any of the other
         * builder setters, only the port number is updated by this method.
         * <p>
         * If no socket address or port is specified, the wildcard network
         * interface will be used and a random port will be selected by the
         * system.
         *
         * @param port Socket port number. If 0 is provided, the system will
         *             choose a random port.
         * @return This builder.
         */
        public final B port(final int port) {
            if (socketAddress != null) {
                final var address = socketAddress.getAddress();
                if (address != null) {
                    return socketAddress(address, port);
                }
                final var hostname = socketAddress.getHostName();
                if (hostname != null) {
                    return socketAddress(hostname, port);
                }
            }
            return socketAddress(new InetSocketAddress(port));
        }

        public final B keyStore(final X509KeyStore keyStore) {
            this.keyStore = keyStore;
            return self();
        }

        public final B trustStore(final X509TrustStore trustStore) {
            this.trustStore = trustStore;
            return self();
        }

        /**
         * Explicitly enables insecure mode for this Arrowhead system.
         * <p>
         * In insecure mode, no cryptography is used to establish identities or
         * connections between systems. Usage of this mode is not advised for
         * most kinds of production scenarios.
         * <p>
         * It is an error to provide a key store or a trust store via
         * {@link #keyStore(X509KeyStore)} or
         * {@link #trustStore(X509TrustStore)} if insecure mode is enabled.
         *
         * @return This builder.
         */
        public final B insecure() {
            this.isInsecure = true;
            return self();
        }

        public final B scheduler(final Scheduler scheduler) {
            this.scheduler = scheduler;
            return self();
        }

        public abstract AS build();
    }
}
