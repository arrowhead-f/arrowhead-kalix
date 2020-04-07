package se.arkalix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.description.ConsumerDescription;
import se.arkalix.internal.ArServer;
import se.arkalix.internal.ArServerRegistry;
import se.arkalix.internal.plugin.PluginNotifier;
import se.arkalix.internal.util.concurrent.NettyScheduler;
import se.arkalix.plugin.Plugin;
import se.arkalix.query.ServiceQuery;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.SystemIdentity;
import se.arkalix.security.identity.TrustStore;
import se.arkalix.util.annotation.ThreadSafe;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Futures;
import se.arkalix.util.concurrent.SchedulerShutdownListener;
import se.arkalix.util.concurrent.Schedulers;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * An Arrowhead Framework (AHF) system.
 */
public class ArSystem {
    private static final Logger logger = LoggerFactory.getLogger(ArSystem.class);

    private final String name;
    private final AtomicReference<InetSocketAddress> localSocketAddress = new AtomicReference<>();
    private final boolean isSecure;
    private final OwnedIdentity identity;
    private final TrustStore trustStore;
    private final NettyScheduler scheduler;
    private final SchedulerShutdownListener schedulerShutdownListener;
    private final PluginNotifier pluginNotifier;

    private final ProviderDescription description;
    private final Set<ArServer> servers = Collections.synchronizedSet(new HashSet<>());
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private ArSystem(final Builder builder) {
        localSocketAddress.setPlain(Objects.requireNonNullElseGet(builder.socketAddress, () ->
            new InetSocketAddress(0)));

        if (builder.isSecure) {
            isSecure = true;
            if (builder.identity == null || builder.trustStore == null) {
                throw new IllegalArgumentException("Expected identity and " +
                    "trustStore; required in secure mode");
            }
            identity = builder.identity;
            trustStore = builder.trustStore;

            final var name0 = identity.name();
            if (builder.name != null && !Objects.equals(builder.name, name0)) {
                throw new IllegalArgumentException("Expected name to either " +
                    "not be provided or to match the system certificate " +
                    "name of the provided identity; \"" + builder.name +
                    "\" != \"" + name0 + "\"");
            }
            name = name0;
        }
        else {
            isSecure = false;
            if (builder.identity != null || builder.trustStore != null) {
                throw new IllegalArgumentException("Unexpected identity or " +
                    "trustStore; not permitted in insecure mode");
            }
            identity = null;
            trustStore = null;

            if (builder.name == null || builder.name.length() == 0) {
                throw new IllegalArgumentException("Expected name; required " +
                    "in insecure mode");
            }
            name = builder.name;
        }

        description = new ProviderDescription(name, localSocketAddress.get(), isSecure
            ? identity.publicKey()
            : null);

        scheduler = (NettyScheduler) Schedulers.fixed();
        schedulerShutdownListener = (scheduler) -> shutdown()
            .onFailure(fault -> {
                if (logger.isErrorEnabled()) {
                    logger.error("Shutdown of \"" + name + "\" failed", fault);
                }
            });
        scheduler.addShutdownListener(schedulerShutdownListener);

        pluginNotifier = new PluginNotifier(this, builder.plugins != null
            ? builder.plugins
            : Collections.emptyList());
    }

    /**
     * @return Human and machine-readable name of this system.
     */
    @ThreadSafe
    public String name() {
        return name;
    }

    /**
     * @return Address and port number of locally bound network interface.
     */
    @ThreadSafe
    public InetSocketAddress localSocketAddress() {
        return localSocketAddress.get();
    }

    /**
     * @return Local network interface address.
     */
    @ThreadSafe
    public InetAddress localAddress() {
        return localSocketAddress().getAddress();
    }

    /**
     * @return Port through which this system exposes its provided services.
     */
    @ThreadSafe
    public int localPort() {
        return localSocketAddress().getPort();
    }

    /**
     * @return {@code true} if and only if this system is configured to run
     * in secure mode.
     */
    @ThreadSafe
    public final boolean isSecure() {
        return isSecure;
    }

    /**
     * @return The cryptographic identity of this system.
     * @throws IllegalStateException If this system is not running in secure
     *                               mode.
     */
    @ThreadSafe
    public final OwnedIdentity identity() {
        if (!isSecure) {
            throw new IllegalStateException("System \"" + name() + "\" not " +
                "in secure mode");
        }
        return identity;
    }

    /**
     * @return Trust store, which contains certificates trusted by this system.
     * @throws IllegalStateException If this system is not running in secure
     *                               mode.
     */
    @ThreadSafe
    public final TrustStore trustStore() {
        if (!isSecure) {
            throw new IllegalStateException("System \"" + name() + "\" not " +
                "in secure mode");
        }
        return trustStore;
    }

    /**
     * @return Description of this system.
     */
    @ThreadSafe
    public ProviderDescription description() {
        return description;
    }

    /**
     * Creates new query useful for resolving services provided by other
     * Arrowhead systems.
     * <p>
     * Resolution will always complete with an empty result unless a {@link
     * Plugin} has been provided at {@link Builder#plugins(Collection) system
     * creation} that is able to perform service lookup.
     *
     * @return Service query builder.
     * @see ServiceQuery
     */
    @ThreadSafe
    public ServiceQuery consume() {
        return new ServiceQuery(this, pluginNotifier::onServiceQueried);
    }

    /**
     * @return Stream of descriptions of all services currently provided by
     * this system.
     */
    @ThreadSafe
    public Stream<ServiceDescription> providedServices() {
        return servers.stream()
            .flatMap(server -> server.providedServices()
                .map(ArServiceHandle::description));
    }

    /**
     * Registers given {@code service} with this system, eventually making it
     * accessible to remote Arrowhead systems.
     * <p>
     * It is only acceptable to register any given service once, unless it is
     * first dismissed via the returned handle.
     *
     * @param service Service to be provided by this system.
     * @return Future completed with service handle only if the service can be
     * provided.
     * @throws NullPointerException If {@code service} is {@code null}.
     */
    @ThreadSafe
    public Future<ArServiceHandle> provide(final ArService service) {
        Objects.requireNonNull(service, "Expected service");

        if (isShuttingDown.get()) {
            return Future.failure(cannotProvideServiceShuttingDownException());
        }

        for (final var server : servers) {
            if (server.canProvide(service)) {
                return server.provide(service);
            }
        }

        final var serverConstructor = ArServerRegistry.get(service.getClass())
            .orElseThrow(() -> new IllegalArgumentException("" +
                "No Arrowhead server exists for services of type \"" +
                service.getClass() + "\""));

        return serverConstructor.create(this, pluginNotifier)
            .flatMap(server -> {
                if (isShuttingDown.get()) {
                    return server.close().fail(cannotProvideServiceShuttingDownException());
                }
                localSocketAddress.set(server.localSocketAddress());
                servers.add(server);
                return server.provide(service);
            });
    }

    private Throwable cannotProvideServiceShuttingDownException() {
        return new IllegalStateException("Cannot provide service; system is shutting down");
    }

    /**
     * Initiates system shutdown, causing all of its services to be dismissed.
     * <p>
     * System shutdown is irreversible, meaning that the system cannot be used
     * to provide more services after this method has been invoked.
     *
     * @return Future completed when shutdown is finished.
     */
    @ThreadSafe
    public Future<?> shutdown() {
        if (isShuttingDown.getAndSet(true)) {
            return Future.done();
        }
        scheduler.removeShutdownListener(schedulerShutdownListener);
        return Futures.serialize(servers.stream().map(ArServer::close))
            .mapResult(result -> {
                pluginNotifier.clear();
                servers.clear();
                return result;
            });
    }

    /**
     * @return {@code true} only if this system is currently in the process of,
     * or already has, shut down.
     */
    @ThreadSafe
    public boolean isShuttingDown() {
        return isShuttingDown.get() || scheduler.isShuttingDown();
    }

    /**
     * Builder useful for creating instances of the {@link ArSystem}
     * class.
     */
    public static class Builder {
        private String name;
        private InetSocketAddress socketAddress;
        private OwnedIdentity identity;
        private TrustStore trustStore;
        private boolean isSecure = true;
        private Collection<Plugin> plugins;

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
         * provided via {@link #identity(OwnedIdentity)} is used as name.
         * <p>
         * If not running in secure mode, the name must be specified
         * explicitly. Avoid picking names that contain whitespace or anything
         * but alphanumeric ASCII characters and dashes, as mandated for DNS
         * names by RFC 1035, Section 2.3.1.
         *
         * @param name Name of this system.
         * @return This builder.
         * @see SystemIdentity More about Arrowhead certifiate names
         * @see <a href="https://tools.ietf.org/html/rfc1035#section-2.3.1">RFC 1035, Section 2.3.1</a>
         */
        public Builder name(final String name) {
            this.name = name;
            return this;
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
        public Builder localAddress(final InetAddress address) {
            if (socketAddress != null) {
                return localAddressPort(address, socketAddress.getPort());
            }
            return localSocketAddress(new InetSocketAddress(address, 0));
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
        public Builder localSocketAddress(final InetSocketAddress socketAddress) {
            this.socketAddress = socketAddress;
            return this;
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
        public Builder localAddressPort(final InetAddress socketAddress, final int port) {
            return localSocketAddress(new InetSocketAddress(socketAddress, port));
        }

        /**
         * Sets the network interface by hostname and socket port number to be
         * used by this system when providing its services.
         * <p>
         * Calling this method with a non-null {@code hostname} will cause a
         * blocking hostname resolution attempt to take place.
         * <p>
         * If no socket hostname or port is specified, the wildcard network
         * interface will be used and a random port will be selected by the
         * system.
         *
         * @param hostname DNS hostname associated with preferred network
         *                 interface.
         * @param port     Socket port number. If 0 is provided, the system
         *                 will choose a random port.
         * @return This builder.
         */
        public Builder localHostnamePort(final String hostname, final int port) {
            return localSocketAddress(new InetSocketAddress(hostname, port));
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
        public Builder localPort(final int port) {
            if (socketAddress != null) {
                final var address = socketAddress.getAddress();
                if (address != null) {
                    return localAddressPort(address, port);
                }
                final var hostname = socketAddress.getHostName();
                if (hostname != null) {
                    return localHostnamePort(hostname, port);
                }
            }
            return localSocketAddress(new InetSocketAddress(port));
        }

        /**
         * Sets owned identity to use for representing the created system and
         * its services.
         * <p>
         * An {@link ArSystem} either must have or must not have an owned
         * identity, depending on whether it is running in secure mode or not,
         * respectively.
         *
         * @param identity Owned identity to use.
         * @return This builder.
         */
        public final Builder identity(final OwnedIdentity identity) {
            this.identity = identity;
            return this;
        }

        /**
         * Sets trust store to use for determining what systems are trusted to
         * be communicated by the created system.
         * <p>
         * An {@link ArSystem} either must have or must not have a
         * trust store, depending on whether it is running in secure mode or
         * not, respectively.
         *
         * @param trustStore Trust store to use.
         * @return This builder.
         */
        public final Builder trustStore(final TrustStore trustStore) {
            this.trustStore = trustStore;
            return this;
        }

        /**
         * Explicitly enables insecure mode for this Arrowhead system.
         * <p>
         * In insecure mode, no cryptography is used to establish identities or
         * connections between systems. Usage of this mode is not advised for
         * most kinds of production scenarios.
         * <p>
         * It is an error to provide an owned identity or a trust store via
         * {@link #identity(OwnedIdentity)} or
         * {@link #trustStore(TrustStore)} if insecure mode is enabled.
         *
         * @return This builder.
         */
        public final Builder insecure() {
            this.isSecure = false;
            return this;
        }

        /**
         * Sets {@link Plugin plugins} to be used by this system.
         * <p>
         * Plugins are useful for listening and reacting to system life-cycle
         * event, and can be used to inject security functionality into
         * services, automatically spin up services, among other things.
         *
         * @param plugins Desired system plugins.
         * @return This builder.
         */
        public Builder plugins(final Plugin... plugins) {
            return plugins(List.of(plugins));
        }

        /**
         * Sets {@link Plugin plugins} to be used by this system.
         * <p>
         * Plugins are useful for listening and reacting to system life-cycle
         * event, and can be used to inject security functionality into
         * services, automatically spin up services, among other things.
         *
         * @param plugins Desired system plugins.
         * @return This builder.
         */
        public Builder plugins(final Collection<Plugin> plugins) {
            this.plugins = plugins;
            return this;
        }

        /**
         * @return New {@link ArSystem}.
         */
        public ArSystem build() {
            return new ArSystem(this);
        }
    }
}
