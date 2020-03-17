package eu.arrowhead.kalix;

import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.internal.AhfServer;
import eu.arrowhead.kalix.internal.net.http.service.HttpServer;
import eu.arrowhead.kalix.internal.plugin.PluginNotifier;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.plugin.Plugin;
import eu.arrowhead.kalix.security.X509ArrowheadName;
import eu.arrowhead.kalix.security.X509Certificates;
import eu.arrowhead.kalix.security.X509KeyStore;
import eu.arrowhead.kalix.security.X509TrustStore;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.FutureScheduler;
import eu.arrowhead.kalix.util.concurrent.FutureSchedulerShutdownListener;
import eu.arrowhead.kalix.util.concurrent.Futures;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * An Arrowhead Framework (AHF) system.
 */
public class AhfSystem {
    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTING = 1;
    private static final int STATE_STARTED = 2;
    private static final int STATE_STOPPING = 3;
    private static final int STATE_SHUTTING_DOWN = 4;

    private final AtomicInteger state = new AtomicInteger(STATE_STOPPED);

    private final String name;
    private final AtomicReference<InetSocketAddress> localSocketAddress = new AtomicReference<>();
    private final boolean isSecure;
    private final X509KeyStore keyStore;
    private final X509TrustStore trustStore;
    private final PluginNotifier pluginNotifier;
    private final FutureScheduler scheduler;
    private final FutureSchedulerShutdownListener schedulerShutdownListener;

    private final Set<AhfServer> servers = Collections.synchronizedSet(new HashSet<>());

    private AhfSystem(final Builder builder) {
        var name = builder.name;
        localSocketAddress.setPlain(Objects.requireNonNullElseGet(builder.socketAddress, () ->
            new InetSocketAddress(0)));

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
            final var systemName = arrowheadName0.system();
            if (systemName.isEmpty()) {
                throw new IllegalArgumentException("No Arrowhead system " +
                    "name in keyStore certificate common name \"" +
                    arrowheadName0 + "\"; that name must match " +
                    "`<system>.<cloud>.<company>.arrowhead.eu`");
            }
            this.name = systemName.get();
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

        pluginNotifier = new PluginNotifier(this, builder.plugins != null
            ? builder.plugins
            : Collections.emptyList());

        scheduler = builder.scheduler != null
            ? builder.scheduler
            : FutureScheduler.getDefault();

        schedulerShutdownListener = (scheduler, timeout) -> shutdown()
            .onFailure(Throwable::printStackTrace); // TODO: Log properly.

        scheduler.addShutdownListener(schedulerShutdownListener);
    }

    /**
     * @return Human and machine-readable name of this system.
     */
    public String name() {
        return name;
    }

    /**
     * @return Local network interface address.
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
     * @return Unmodifiable list of descriptions of all services currently
     * provided by this system.
     */
    public List<ServiceDescription> providedServices() {
        return servers.stream()
            .flatMap(server -> server.providedServices()
                .map(AhfServiceHandle::description))
            .collect(Collectors.toUnmodifiableList());
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
    public Future<AhfServiceHandle> provide(final AhfService service) {
        Objects.requireNonNull(service, "Expected service");

        switch (state.get()) {
        case STATE_STOPPED:
        case STATE_STARTING:
        case STATE_STARTED:
        case STATE_STOPPING:
            break;

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Cannot provide service; system is shutting down"));

        default:
            return Future.failure(new IllegalStateException("System in illegal state: " + state.get()));
        }

        for (final var server : servers) {
            if (server.canProvide(service)) {
                return server.provide(service);
            }
        }

        final AhfServer server;
        if (service instanceof HttpService) {
            server = new HttpServer(this, pluginNotifier);
        }
        else {
            throw new IllegalArgumentException("No server available for services of type " + service.getClass());
        }

        return server.start().flatMap(socketAddress -> {
            localSocketAddress.set(socketAddress);
            servers.add(server);
            return server.provide(service);
        });
    }

    /**
     * Starts system, making all of its services available to remote systems.
     */
    public Future<?> start() {
        switch (state.compareAndExchange(STATE_STOPPED, STATE_STARTING)) {
        case STATE_STOPPED:
            break;

        case STATE_STARTING:
            return Future.failure(new IllegalStateException("Already starting"));

        case STATE_STARTED:
            return Future.done();

        case STATE_STOPPING:
            return Future.failure(new IllegalStateException("Cannot start while stopping"));

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Cannot start while shutting down"));

        default:
            return Future.failure(new IllegalStateException("System in illegal state: " + state.get()));
        }
        return pluginNotifier.onSystemStarted()
            .flatMap(ignored -> Futures.serialize(servers.stream().map(AhfServer::start))
                .map(values -> {
                    if (values.size() > 0) {
                        localSocketAddress.set(values.get(0));
                    }
                    return null;
                }))
            .flatMapResult(result -> {
                state.compareAndSet(STATE_STARTING, STATE_STARTED);
                if (result.isFailure()) {
                    return stop().map(ignored -> null);
                }
                return Future.of(Result.done());
            });
    }

    /**
     * Stops system, making all of its services unavailable to remote systems.
     *
     * @return Future completed when stopping finishes. If this system is or
     * has been providing services with different application-level protocols,
     * the future might be failed with an exception containing suppressed
     * exceptions.
     */
    public Future<?> stop() {
        switch (state.compareAndExchange(STATE_STARTED, STATE_STOPPING)) {
        case STATE_STOPPED:
            return Future.done();

        case STATE_STARTING:
            return Future.failure(new IllegalStateException("Cannot stop while starting"));

        case STATE_STARTED:
            break;

        case STATE_STOPPING:
            return Future.failure(new IllegalStateException("Already stopping"));

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Cannot stop while shutting down"));

        default:
            return Future.failure(new IllegalStateException("System in illegal state: " + state.get()));
        }
        return pluginNotifier.onSystemStopped()
            .flatMap(ignored -> Futures.serialize(servers.stream().map(AhfServer::stop).iterator()))
            .mapResult(result -> {
                state.compareAndSet(STATE_STOPPING, STATE_STOPPED);
                return result;
            });
    }

    public Future<?> shutdown() {
        switch (state.getAndSet(STATE_SHUTTING_DOWN)) {
        case STATE_STOPPED:
        case STATE_STARTING:
        case STATE_STARTED:
        case STATE_STOPPING:
            break;

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Already shutting down"));

        default:
            return Future.failure(new IllegalStateException("System in illegal state: " + state.get()));
        }

        scheduler.removeShutdownListener(schedulerShutdownListener);

        return pluginNotifier.onSystemStopped()
            .flatMap(ignored -> Futures.serialize(servers.stream().map(AhfServer::shutdown).iterator()))
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
    public boolean isShuttingDown() {
        return state.get() == STATE_SHUTTING_DOWN || scheduler.isShuttingDown();
    }

    /**
     * Builder useful for creating instances of the {@link AhfSystem}
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
         * An {@link AhfSystem} either must have or must not have a
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
         * An {@link AhfSystem} either must have or must not have a
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
         * @return New {@link AhfSystem}.
         */
        public AhfSystem build() {
            return new AhfSystem(this);
        }
    }
}
