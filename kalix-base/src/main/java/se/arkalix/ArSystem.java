package se.arkalix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.description.ProviderDescription;
import se.arkalix.description.ServiceDescription;
import se.arkalix.internal.ArServer;
import se.arkalix.internal.ArServerRegistry;
import se.arkalix.internal.plugin.PluginNotifier;
import se.arkalix.internal.util.concurrent.FutureSynchronizer;
import se.arkalix.internal.util.concurrent.NettyScheduler;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.query.ServiceQuery;
import se.arkalix.security.NotSecureException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An Arrowhead Framework (AHF) system.
 */
@SuppressWarnings("unused")
public class ArSystem {
    private static final Logger logger = LoggerFactory.getLogger(ArSystem.class);

    private final String name;
    private final InetSocketAddress localSocketAddress;
    private final boolean isSecure;
    private final OwnedIdentity identity;
    private final TrustStore trustStore;
    private final NettyScheduler scheduler;
    private final SchedulerShutdownListener schedulerShutdownListener;
    private final PluginNotifier pluginNotifier;

    private final ArServiceCache consumedServices;
    private final ProviderDescription description;
    private final Set<ArServer> servers = new HashSet<>();
    private final FutureSynchronizer<ArServiceHandle> serviceSynchronizer = new FutureSynchronizer<>();
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private Map<Class<? extends Plugin>, PluginFacade> pluginClassToFacade = null;

    private ArSystem(final Builder builder) {
        localSocketAddress = Objects.requireNonNullElseGet(builder.socketAddress, () -> new InetSocketAddress(0));

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

        description = new ProviderDescription(name, localSocketAddress, isSecure
            ? identity.publicKey()
            : null);

        consumedServices = Objects.requireNonNullElseGet(builder.serviceCache,
            ArServiceCache::withDefaultEntryLifetimeLimit);

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

    Future<?> attachPlugins() {
        return pluginNotifier.onAttach()
            .ifSuccess(pluginClassToFacade -> this.pluginClassToFacade = pluginClassToFacade);
    }

    /**
     * Gets human and machine-readable name of this system.
     * <p>
     * If this system is running in secure mode, the returned name will be the
     * system name in its {@link se.arkalix.security.identity certificate}. It
     * will otherwise be a {@link Builder#name(String) configured} name.
     *
     * @return System name.
     */
    @ThreadSafe
    public String name() {
        return name;
    }

    /**
     * Gets the Internet socket address this system has been {@link
     * Builder#localSocketAddress(InetSocketAddress) configured} to use.
     *
     * @return Address/hostname and port number of locally bound network
     * interface.
     */
    @ThreadSafe
    public InetSocketAddress localSocketAddress() {
        return localSocketAddress;
    }

    /**
     * Gets the Internet address this system has been {@link
     * Builder#localSocketAddress(InetSocketAddress) configured} to use.
     *
     * @return Local network interface address.
     */
    @ThreadSafe
    public InetAddress localAddress() {
        return localSocketAddress().getAddress();
    }

    /**
     * Gets the Internet port this system has been {@link
     * Builder#localSocketAddress(InetSocketAddress) configured} to use.
     *
     * @return Port through which this system exposes its provided services.
     */
    @ThreadSafe
    public int localPort() {
        return localSocketAddress().getPort();
    }

    /**
     * Determines whether or not this system is running in {@link
     * se.arkalix.security secure mode}.
     *
     * @return {@code true} if and only if this system is configured to run
     * in {@link se.arkalix.security secure mode}.
     */
    @ThreadSafe
    public final boolean isSecure() {
        return isSecure;
    }

    /**
     * Gets the {@link se.arkalix.security.identity cryptographic identity}
     * this system has been {@link Builder#identity(OwnedIdentity) configured}
     * to use.
     *
     * @return The cryptographic identity of this system.
     * @throws NotSecureException If this system is not running in secure
     *                            mode.
     */
    @ThreadSafe
    public final OwnedIdentity identity() {
        if (!isSecure) {
            throw new NotSecureException("System \"" + name() + "\" not " +
                "in secure mode");
        }
        return identity;
    }

    /**
     * Gets the {@link se.arkalix.security.identity trust store} this system
     * has been {@link Builder#trustStore(TrustStore)} configured} to use.
     *
     * @return Trust store, which contains certificates trusted by this system.
     * @throws NotSecureException If this system is not running in secure
     *                            mode.
     */
    @ThreadSafe
    public final TrustStore trustStore() {
        if (!isSecure) {
            throw new NotSecureException("System \"" + name() + "\" not " +
                "in secure mode");
        }
        return trustStore;
    }

    /**
     * Gets description of this system as a {@link se.arkalix service provider}.
     *
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
        return new ServiceQuery(this, this::query);
    }

    private Future<Set<ServiceDescription>> query(final ServiceQuery query) {
        final var matchingServices = consumedServices.getAll()
            .filter(query::matches)
            .collect(Collectors.toUnmodifiableSet());

        if (!matchingServices.isEmpty()) {
            return Future.success(matchingServices);
        }

        return pluginNotifier.onServiceQueried(query)
            .ifSuccess(consumedServices::update)
            .map(services -> services.stream()
                .filter(query::matches)
                .collect(Collectors.toUnmodifiableSet()));
    }

    /**
     * Cache of, potentially or previously, consumed services.
     * <p>
     * Searches made in this cache will never trigger any lookup in a remote
     * service registry. The cache is strictly local. It is, however, updated
     * whenever a {@link #consume()} operation resolves in a remote lookup. For
     * remote lookup to work, A {@link se.arkalix.plugin.Plugin plugin} must be
     * provided at {@link ArSystem} creation that supports lookup.
     *
     * @return Service description cache containing services that this system
     * has, or has considered to, consume.
     */
    @ThreadSafe
    public ArServiceCache consumedServices() {
        return consumedServices;
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
            return Future.failure(cannotProvideServiceShuttingDownException(service));
        }

        return serviceSynchronizer.execute(() -> {
            synchronized (servers) {
                for (final var server : servers) {
                    if (server.canProvide(service)) {
                        return server.provide(service);
                    }
                }
            }
            return ArServerRegistry.get(service.getClass())
                .orElseThrow(() -> new IllegalArgumentException("" +
                    "No Arrowhead server exists for services of type \"" +
                    service.getClass() + "\"; cannot provide service \"" +
                    service.name() + "\""))
                .create(this, pluginNotifier)
                .flatMap(server -> {
                    if (isShuttingDown.get()) {
                        return server.close().<ArServiceHandle>fail(
                            cannotProvideServiceShuttingDownException(service));
                    }
                    synchronized (servers) {
                        servers.add(server);
                    }
                    return server.provide(service);
                });
        });
    }

    private Throwable cannotProvideServiceShuttingDownException(final ArService service) {
        return new IllegalStateException("System is shutting down; cannot " +
            "provide service \"" + service.name() + "\"");
    }

    /**
     * Gets descriptions of all services currently provided by this system.
     *
     * @return Stream of service descriptions.
     */
    @ThreadSafe
    public Stream<ServiceDescription> providedServices() {
        if (isShuttingDown.get()) {
            return Stream.empty();
        }
        synchronized (servers) {
            return servers.stream()
                .flatMap(server -> server.providedServices()
                    .map(ArServiceHandle::description));
        }
    }

    /**
     * Gets {@link PluginFacade} associated with the identified system {@link
     * Plugin}, if the identified plugin is attached to this system and the
     * plugin explicitly provides a plugin facade.
     * <p>
     * A plugin facade allows for some attached system plugin to be interacted
     * with. Interfaces returned by this method must be casted into some
     * concrete type, which should be documented by the identified plugin, to
     * become useful.
     *
     * @param pluginClass The class associated with the desired plugin facade.
     * @return Plugin facade associated with a {@link
     * Builder#plugins(Plugin...) plugin provided at system creation}, if the
     * plugin in question provides such a facade.
     */
    @ThreadSafe
    public Optional<PluginFacade> pluginFacadeOf(final Class<? extends Plugin> pluginClass) {
        return Optional.ofNullable(pluginClassToFacade.get(pluginClass));
    }

    /**
     * Initiates system shutdown, causing all of its services to be dismissed.
     * <p>
     * System shutdown is irreversible, meaning that the system cannot be used
     * to provide more services after this method has been invoked.
     * <p>
     * Note that shutting all running systems down will not terminate the Java
     * application hosting them, as it will be kept alive by the {@link
     * Schedulers} they used while running. If wanting to shut down the
     * application itself, use {@link java.lang.System#exit(int)}.
     *
     * @return Future completed when shutdown is finished.
     */
    @ThreadSafe
    public Future<?> shutdown() {
        if (isShuttingDown.getAndSet(true)) {
            return Future.done();
        }
        scheduler.removeShutdownListener(schedulerShutdownListener);
        synchronized (servers) {
            return Futures.serialize(servers.stream().map(ArServer::close))
                .mapResult(result -> {
                    pluginNotifier.onDetach();
                    synchronized (servers) {
                        servers.clear();
                    }
                    return result;
                });
        }
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
        private ArServiceCache serviceCache;

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
         * Explicitly enables {@link se.arkalix.security insecure mode} for
         * this Arrowhead system.
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
         * Sets {@link ArServiceCache service cache} to be used by this system
         * for storing information about remote services of interest. If not
         * provided, a such with a {@link
         * ArServiceCache#withDefaultEntryLifetimeLimit() default entry
         * expiration time} will be used.
         * <p>
         * After system instance creation, the cache will be available via the
         * {@link ArSystem#consumedServices()} method.
         *
         * @param serviceCache Desired service cache.
         * @return This builder.
         */
        public Builder serviceCache(final ArServiceCache serviceCache) {
            this.serviceCache = serviceCache;
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
         * Finalizes construction of new {@link ArSystem} and then blocks the
         * current thread until all of its {@link #plugins(Plugin...) plugins}
         * have been attached.
         *
         * @return New {@link ArSystem}.
         */
        public ArSystem build() {
            final var system = new ArSystem(this);
            try {
                system.attachPlugins().await();
            }
            catch (final InterruptedException exception) {
                throw new RuntimeException("Failed to attach system \"" + system.name() + "\" plugins", exception);
            }
            return system;
        }

        /**
         * Finalizes construction of new {@link ArSystem} and then attaches all
         * of its {@link #plugins(Plugin...) plugins} asynchronously.
         *
         * @return Future completed with a new {@link ArSystem} if such could
         * be constructed and its plugins attached.
         */
        public Future<ArSystem> buildAsync() {
            try {
                final var system = new ArSystem(this);
                return system.attachPlugins()
                    .pass(system);
            }
            catch (final Throwable throwable) {
                return Future.failure(throwable);
            }
        }
    }
}
