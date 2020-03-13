package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.internal.ArrowheadServer;
import eu.arrowhead.kalix.internal.net.http.HttpArrowheadServer;
import eu.arrowhead.kalix.net.http.service.HttpArrowheadService;
import eu.arrowhead.kalix.plugin.Plug;
import eu.arrowhead.kalix.plugin.Plugin;
import eu.arrowhead.kalix.security.X509ArrowheadName;
import eu.arrowhead.kalix.security.X509Certificates;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.Results;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.FutureScheduler;
import eu.arrowhead.kalix.util.concurrent.Futures;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * An arrowhead system.
 */
public class ArrowheadSystem {
    private final String name;
    private final AtomicReference<InetSocketAddress> localSocketAddress = new AtomicReference<>();
    private final boolean isSecure;
    private final X509KeyStore keyStore;
    private final X509TrustStore trustStore;
    private final Map<Plug, Plugin> plugins;
    private final FutureScheduler scheduler;
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private final Map<String, ServiceDescription> serviceDescriptions = new ConcurrentSkipListMap<>();
    private final Set<ArrowheadServer> servers = new CopyOnWriteArraySet<>();

    private ArrowheadSystem(final Builder builder) {
        var name = builder.name;
        localSocketAddress.setPlain(Objects.requireNonNullElseGet(builder.socketAddress, () ->
            new InetSocketAddress(0)));

        if (builder.scheduler == null) {
            scheduler = FutureScheduler.getDefault();
        }
        else {
            scheduler = builder.scheduler;
        }

        if (builder.isSecure) {
            isSecure = true;
            if (builder.keyStore == null || builder.trustStore == null) {
                throw new IllegalArgumentException("Expected keyStore and " +
                    "trustStore; required in secure mode");
            }
            keyStore = builder.keyStore;
            trustStore = builder.trustStore;

            final var arrowheadName = X509Certificates.subjectArrowheadNameOf(keyStore.certificate());
            if (arrowheadName.isEmpty()) {
                throw new IllegalArgumentException("No subject common name " +
                    "in keyStore certificate; required to determine system " +
                    "name");
            }
            final var arrowheadName0 = arrowheadName.get();
            final var system = arrowheadName0.system();
            if (system.isEmpty()) {
                throw new IllegalArgumentException("No Arrowhead system " +
                    "name in keyStore certificate common name \"" +
                    arrowheadName0 + "\"; that name must match " +
                    "`<system>.<cloud>.<company>.arrowhead.eu`");
            }
            this.name = system.get();
        }
        else {
            isSecure = false;
            if (builder.keyStore != null || builder.trustStore != null) {
                throw new IllegalArgumentException("Unexpected keyStore or " +
                    "trustStore; not permitted in insecure mode");
            }
            keyStore = null;
            trustStore = null;

            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("Expected name; required " +
                    "in insecure mode");
            }
            this.name = name;
        }

        final var self = this;
        plugins = builder.plugins == null
            ? Collections.emptyMap()
            : builder.plugins.stream().collect(Collectors.toConcurrentMap(plugin ->
                new Plug() {
                    @Override
                    public void detach() {
                        plugins.remove(this);
                    }

                    @Override
                    public ArrowheadSystem system() {
                        return self;
                    }

                    @Override
                    public boolean isSystemShuttingDown() {
                        return isShuttingDown.get();
                    }
                },
            plugin -> plugin)
        );
        for (final var entry : plugins.entrySet()) {
            entry.getValue().onAttach(entry.getKey());
        }
    }

    /**
     * @return Human and machine-readable name of this system.
     */
    public String name() {
        return name;
    }

    /**
     * @return Network interface address.
     */
    public InetAddress localAddress() {
        return localSocketAddress.get().getAddress();
    }

    /**
     * @return Port through which this system exposes its provided services.
     */
    public int localPort() {
        return localSocketAddress.get().getPort();
    }

    /**
     * @return {@code true} if and only if this system is configured to run
     * in secure mode.
     */
    public final boolean isSecure() {
        return isSecure;
    }

    /**
     * @return Key store, which represents the identity of this system.
     * @throws UnsupportedOperationException If this system is not running in
     *                                       secure mode.
     */
    public final X509KeyStore keyStore() {
        if (!isSecure) {
            throw new UnsupportedOperationException("System \"" + name() + "\" not in secure mode");
        }
        return keyStore;
    }

    /**
     * @return Trust store, which contains certificates trusted by this system.
     * @throws UnsupportedOperationException If this system is not running in
     *                                       secure mode.
     */
    public final X509TrustStore trustStore() {
        if (!isSecure) {
            throw new UnsupportedOperationException("System \"" + name() + "\" not in secure mode");
        }
        return trustStore;
    }

    /**
     * @return Scheduler being used to handle asynchronous task execution.
     */
    public FutureScheduler scheduler() {
        return scheduler;
    }

    /**
     * @return Unmodifiable collection of descriptions representing all
     * services currently provided, or are in the process of becoming provided,
     * by this system.
     */
    public Collection<ServiceDescription> providedServices() {
        return Collections.unmodifiableCollection(serviceDescriptions.values());
    }

    /**
     * Registers given {@code service} with system and makes it accessible to
     * other systems.
     * <p>
     * Calling this method with a service that is already being provided has no
     * effect.
     *
     * @param service Service to be provided by this system.
     * @return Future completed when the service is visible to other systems.
     * @throws NullPointerException  If {@code service} is {@code null}.
     * @throws IllegalStateException If {@code service} configuration conflicts
     *                               with an already provided service.
     */
    public Future<?> provideService(final ArrowheadService service) {
        Objects.requireNonNull(service, "Expected service");

        alreadyExists:
        {
            for (final var server : servers) {
                if (server.canProvide(service)) {
                    final var existingDescription = serviceDescriptions.computeIfAbsent(service.name(), ignored -> {
                        server.provideService(service);
                        return service.describe();
                    });
                    if (existingDescription != null) {
                        break alreadyExists;
                    }
                    return Future.done();
                }
            }

            final ArrowheadServer server;
            if (service instanceof HttpArrowheadService) {
                server = new HttpArrowheadServer(this);
            }
            else {
                throw new IllegalArgumentException("No server available for services of type " + service.getClass());
            }
            if (serviceDescriptions.computeIfAbsent(service.name(), ignored -> service.describe()) != null) {
                break alreadyExists;
            }
            return server.start()
                .map(socketAddress -> {
                    localSocketAddress.set(socketAddress);
                    server.provideService(service);
                    servers.add(server);
                    return null;
                })
                .mapFault(fault -> {
                    serviceDescriptions.remove(service.name());
                    return fault;
                });
        }
        throw new IllegalStateException("A service named \"" + service.name() +
            "\" is already provided");
    }

    /**
     * Deregisters given {@code service} from this system, making it
     * inaccessible to other systems.
     * <p>
     * Calling this method with a service that is not currently provided has no
     * effect.
     *
     * @param service Service to no longer be provided by this system.
     * @throws NullPointerException If {@code service} is {@code null}.
     */
    public void dismissService(final ArrowheadService service) {
        Objects.requireNonNull(service, "Expected service");
        if (serviceDescriptions.remove(service.name()) != null) {
            for (final var server : servers) {
                if (server.canProvide(service)) {
                    server.dismissService(service);
                    return;
                }
            }
        }
    }

    /**
     * Dismisses all services and stops system.
     * <p>
     * The system turns on again if provided with new services. It is not safe,
     * however, to provide new services until after the returned future
     * completes.
     *
     * @return Future completed when stopping finishes. If this system is or
     * has been providing services with different application-level protocols,
     * the future might be failed with an exception containing suppressed
     * exceptions.
     */
    public Future<?> stop() {
        serviceDescriptions.clear();
        return Futures.serialize(servers.stream().map(ArrowheadServer::stop).iterator())
            .map(Results::mergeFaults);
    }

    /**
     * Builder useful for creating instances of the {@link ArrowheadSystem}
     * class.
     */
    public static class Builder {
        private String name;
        private InetSocketAddress socketAddress;
        private X509KeyStore keyStore;
        private X509TrustStore trustStore;
        private boolean isSecure = true;
        private List<Plugin> plugins;
        private FutureScheduler scheduler;

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
         * @see X509ArrowheadName More about Arrowhead certifiate names
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
         * Sets key store to use for representing the created system and its
         * services.
         * <p>
         * An {@link ArrowheadSystem} either must have or must not have a
         * key store, depending on whether it is running in secure mode or not,
         * respectively.
         *
         * @param keyStore Key store to use.
         * @return This builder.
         */
        public final Builder keyStore(final X509KeyStore keyStore) {
            this.keyStore = keyStore;
            return this;
        }

        /**
         * Sets trust store to use for determining what systems are trusted to
         * be communicated by the created system.
         * <p>
         * An {@link ArrowheadSystem} either must have or must not have a
         * trust store, depending on whether it is running in secure mode or
         * not, respectively.
         *
         * @param trustStore Trust store to use.
         * @return This builder.
         */
        public final Builder trustStore(final X509TrustStore trustStore) {
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
         * It is an error to provide a key store or a trust store via
         * {@link #keyStore(X509KeyStore)} or
         * {@link #trustStore(X509TrustStore)} if insecure mode is enabled.
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
            return plugins(Arrays.asList(plugins));
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
        public Builder plugins(final List<Plugin> plugins) {
            this.plugins = plugins;
            return this;
        }

        /**
         * Sets scheduler to be used by the created system.
         * <p>
         * If a non-null scheduler is explicitly provided via this method, it
         * becomes the responsibility of the caller to ensure that the
         * scheduler is shut down when no longer in use.
         * <p>
         * If no scheduler is explicitly specified, the one returned by
         * {@link FutureScheduler#getDefault()} is used instead. This means
         * that if several systems are created without schedulers being
         * explicitly set, the systems will all share the same scheduler.
         *
         * @param scheduler Asynchronous task scheduler.
         * @return This builder.
         */
        public Builder scheduler(final FutureScheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        /**
         * @return New {@link ArrowheadSystem}.
         */
        public ArrowheadSystem build() {
            return new ArrowheadSystem(this);
        }
    }
}
