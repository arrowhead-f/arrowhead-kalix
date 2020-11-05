package se.arkalix._internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.ArServiceRecordCache;
import se.arkalix.ArServiceHandle;
import se.arkalix.ArSystem;
import se.arkalix.SystemRecord;
import se.arkalix.ServiceRecord;
import se.arkalix.plugin._internal.PluginNotifier;
import se.arkalix.plugin.Plugin;
import se.arkalix.plugin.PluginFacade;
import se.arkalix.query.ServiceQuery;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.security.identity.OwnedIdentity;
import se.arkalix.security.identity.TrustStore;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Internal
public class DefaultSystem implements ArSystem {
    private static final Logger logger = LoggerFactory.getLogger(ArSystem.class);

    private final String name;
    private final InetSocketAddress localSocketAddress;
    private final boolean isSecure;
    private final OwnedIdentity identity;
    private final TrustStore trustStore;
    private final Scheduler scheduler;
    private final SchedulerShutdownListener schedulerShutdownListener;
    private final PluginNotifier pluginNotifier;

    private final ArServiceRecordCache consumedServices;
    private final Map<Class<? extends ArService>, FutureAnnouncement<ArServer>> servers = new ConcurrentHashMap<>();

    private final SystemRecord description;
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private Map<Class<? extends Plugin>, PluginFacade> pluginClassToFacade = null;

    private DefaultSystem(final Builder builder) {
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

        description = SystemRecord.from(name, isSecure
            ? identity.publicKey()
            : null, localSocketAddress);

        consumedServices = Objects.requireNonNullElseGet(builder.serviceCache,
            ArServiceRecordCache::withDefaultEntryLifetimeLimit);

        scheduler = Schedulers.fixed();
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

    private Future<?> attachPlugins() {
        return pluginNotifier.onAttach()
            .ifSuccess(pluginClassToFacade -> this.pluginClassToFacade = pluginClassToFacade);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public InetSocketAddress socketAddress() {
        return localSocketAddress;
    }

    @Override
    public final boolean isSecure() {
        return isSecure;
    }

    @Override
    public final OwnedIdentity identity() {
        if (!isSecure) {
            throw new SecurityDisabled("System \"" + name() + "\" not " +
                "in secure mode");
        }
        return identity;
    }

    @Override
    public final TrustStore trustStore() {
        if (!isSecure) {
            throw new SecurityDisabled("System \"" + name() + "\" not " +
                "in secure mode");
        }
        return trustStore;
    }

    @Override
    public SystemRecord description() {
        return description;
    }

    @Override
    public ServiceQuery consume() {
        return new ServiceQuery(this, this::query);
    }

    private Future<Set<ServiceRecord>> query(final ServiceQuery query) {
        final var isTraceEnabled = logger.isTraceEnabled();

        if (isTraceEnabled) {
            logger.trace("Executing given {} ...", query);
        }

        final var matchingServices = consumedServices.getAll()
            .filter(query::matches)
            .collect(Collectors.toUnmodifiableSet());

        if (isTraceEnabled) {
            logger.trace("Resolved the following from service cache: {}", matchingServices);
        }

        if (!matchingServices.isEmpty()) {
            if (isTraceEnabled) {
                logger.trace("Service cache contained at least one service " +
                    "matching the given query");
            }
            return Future.success(matchingServices);
        }

        if (isTraceEnabled) {
            logger.trace("Service cache did not contain any service " +
                "matching the given query, delegating query to plugins ...");
        }
        return pluginNotifier.onServiceQueried(query)
            .ifSuccess(services -> {
                if (isTraceEnabled) {
                    logger.trace("Retrieved the following entries from " +
                        "plugins, which will be used to update the service " +
                        "cache: {}", services);
                }
                consumedServices.update(services);
            })
            .map(services -> services.stream()
                .filter(query::matches)
                .collect(Collectors.toUnmodifiableSet()))
            .ifSuccess(services -> {
                if (isTraceEnabled) {
                    logger.trace("The following entries matched the given " +
                        "query: {}", services);
                }
            });
    }

    @Override
    public ArServiceRecordCache consumedServices() {
        return consumedServices;
    }

    @Override
    public Future<ArServiceHandle> provide(final ArService service) {
        Objects.requireNonNull(service, "service");

        if (isShuttingDown.get()) {
            return Future.failure(new IllegalStateException("System is shutting down; cannot " +
                "provide service \"" + service.name() + "\""));
        }

        return servers.computeIfAbsent(service.getClass(), (class_) -> ArServerRegistry.get(class_)
            .orElseThrow(() -> new IllegalArgumentException("" +
                "No Arrowhead server exists for services of type \"" +
                service.getClass() + "\"; cannot provide service \"" +
                service.name() + "\""))
            .create(this, pluginNotifier)
            .toAnnouncement())
            .subscribe()
            .flatMap(server -> server.provide(service));
    }

    @Override
    public Collection<ServiceRecord> providedServices() {
        final var providedServices = new ArrayList<ServiceRecord>();
        for (final var entry : servers.entrySet()) {
            final var announcement = entry.getValue();
            final var optional = announcement.resultIfAvailable();
            if (optional.isEmpty()) {
                continue;
            }
            final var result = optional.get();
            if (result.isFailure()) {
                continue;
            }
            final var server = result.value();
            server.providedServices()
                .stream()
                .map(ArServiceHandle::description)
                .forEach(providedServices::add);
        }
        return providedServices;
    }

    @Override
    public Optional<PluginFacade> pluginFacadeOf(final Class<? extends Plugin> pluginClass) {
        return Optional.ofNullable(pluginClassToFacade.get(pluginClass));
    }

    @Override
    public Future<?> shutdown() {
        if (isShuttingDown.getAndSet(true)) {
            return Future.done();
        }
        scheduler.removeShutdownListener(schedulerShutdownListener);

        final var closingServers = new ArrayList<Future<?>>();

        for (final var entry : servers.entrySet()) {
            final var announcement = entry.getValue();
            final var optional = announcement.resultIfAvailable();
            if (optional.isEmpty()) {
                announcement.cancel(true);
                continue;
            }
            final var result = optional.get();
            if (result.isFailure()) {
                logger.warn("Could not shut down " + entry.getKey() +
                    " server; it never started due to the following " +
                    "exception", result.fault());
                continue;
            }
            final var server = result.value();
            closingServers.add(server.close());
        }

        return Futures.serialize(closingServers)
            .mapResult(result -> {
                pluginNotifier.onDetach();
                servers.clear();
                return result;
            });
    }

    @Override
    public boolean isShuttingDown() {
        return isShuttingDown.get() || scheduler.isShuttingDown();
    }

    @Override
    public String toString() {
        return description.toString();
    }

    @Internal
    public static class Builder {
        private String name;
        private InetSocketAddress socketAddress;
        private OwnedIdentity identity;
        private TrustStore trustStore;
        private boolean isSecure = true;
        private Collection<Plugin> plugins;
        private ArServiceRecordCache serviceCache;

        public void name(final String name) {
            this.name = name;
        }

        public void localAddress(final InetAddress address) {
            if (socketAddress != null) {
                localAddressPort(address, socketAddress.getPort());
            }
            else {
                localSocketAddress(new InetSocketAddress(address, 0));
            }
        }

        public void localSocketAddress(final InetSocketAddress socketAddress) {
            this.socketAddress = socketAddress;
        }

        public void localAddressPort(final InetAddress address, final int port) {
            localSocketAddress(new InetSocketAddress(address, port));
        }

        public void localHostnamePort(final String hostname, final int port) {
            localSocketAddress(new InetSocketAddress(hostname, port));
        }

        public void localPort(final int port) {
            if (socketAddress != null) {
                final var address = socketAddress.getAddress();
                if (address != null) {
                    localAddressPort(address, port);
                }
                else {
                    final var hostname = socketAddress.getHostName();
                    if (hostname != null) {
                        localHostnamePort(hostname, port);
                    }
                }
            }
            else {
                localSocketAddress(new InetSocketAddress(port));
            }
        }

        public final void identity(final OwnedIdentity identity) {
            this.identity = identity;
        }

        public final void trustStore(final TrustStore trustStore) {
            this.trustStore = trustStore;
        }

        public final void insecure() {
            this.isSecure = false;
        }

        public void serviceCache(final ArServiceRecordCache serviceCache) {
            this.serviceCache = serviceCache;
        }

        public void plugins(final Collection<Plugin> plugins) {
            this.plugins = plugins;
        }

        public ArSystem build() {
            final var system = new DefaultSystem(this);
            try {
                system.attachPlugins().await();
            }
            catch (final InterruptedException exception) {
                throw new RuntimeException("Failed to attach system \"" + system.name() + "\" plugins", exception);
            }
            return system;
        }

        public Future<ArSystem> buildAsync() {
            try {
                final var system = new DefaultSystem(this);
                return system.attachPlugins()
                    .pass(system);
            }
            catch (final Throwable throwable) {
                return Future.failure(throwable);
            }
        }
    }
}