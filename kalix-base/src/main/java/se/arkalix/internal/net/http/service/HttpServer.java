package se.arkalix.internal.net.http.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.ArService;
import se.arkalix.ArServiceHandle;
import se.arkalix.ArSystem;
import se.arkalix.ServiceRecord;
import se.arkalix.descriptor.SecurityDescriptor;
import se.arkalix.internal.ArServer;
import se.arkalix.internal.plugin.PluginNotifier;
import se.arkalix.internal.util.concurrent.NettyScheduler;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import se.arkalix.util.concurrent.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class HttpServer implements ArServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private final Set<ArServiceHandle> handles = new HashSet<>();
    private final Map<String, HttpServerService> services = new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    private final PluginNotifier pluginNotifier;
    private final ArSystem system;

    private Channel channel;

    private HttpServer(final ArSystem system, final PluginNotifier pluginNotifier) {
        this.pluginNotifier = Objects.requireNonNull(pluginNotifier, "Expected pluginNotifier");
        this.system = Objects.requireNonNull(system, "Expected system");
    }

    public static Future<ArServer> create(final ArSystem system, final PluginNotifier pluginNotifier) {
        final var server = new HttpServer(system, pluginNotifier);
        try {
            SslContext sslContext = null;
            if (system.isSecure()) {
                final var identity = system.identity();
                sslContext = SslContextBuilder
                    .forServer(identity.privateKey(), identity.chain())
                    .trustManager(system.trustStore().certificates())
                    .clientAuth(ClientAuth.REQUIRE)
                    .startTls(false)
                    .build();
            }

            final var scheduler = (NettyScheduler) Schedulers.fixed();
            final var bootstrap = new ServerBootstrap()
                .group(scheduler.eventLoopGroup())
                .channel(scheduler.serverSocketChannelClass())
                .handler(new LoggingHandler())
                .childHandler(new NettyHttpServiceConnectionInitializer(system, server::getServiceByPath, sslContext));

            return adapt(bootstrap.bind(system.address(), system.port()))
                .map(channel -> {
                    server.channel = channel;
                    return server;
                });
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public boolean canProvide(final ArService service) {
        return service instanceof HttpService;
    }

    @Override
    public Future<ArServiceHandle> provide(final ArService service) {
        Objects.requireNonNull(service, "Expected service");

        if (!(service instanceof HttpService)) {
            throw new IllegalArgumentException("Expected service to be HttpService");
        }

        if (service.accessPolicy().descriptor() == SecurityDescriptor.NOT_SECURE) {
            if (system.isSecure()) {
                throw new IllegalStateException("System \"" + system.name() +
                    "\" is running in secure mode; services with the " +
                    "\"NOT_SECURE\" access policy are not permitted");
            }
        }
        else {
            if (!system.isSecure()) {
                throw new IllegalStateException("System \"" + system.name() +
                    "\" is running in insecure mode; services with other " +
                    "access policies than \"NOT_SECURE\" are not permitted");
            }
        }

        if (isShuttingDown.get()) {
            return Future.failure(cannotProvideServiceShuttingDownException(null));
        }

        return pluginNotifier.onServicePrepared(service).flatMapResult(result0 -> {
            if (result0.isFailure()) {
                return Future.failure(result0.fault());
            }
            final var httpService = new HttpServerService(system, (HttpService) service);
            final var key = httpService.basePath().orElse("/");

            final var existingService = services.putIfAbsent(key, httpService);
            if (existingService != null) {
                return Future.failure(new IllegalStateException("Base path " +
                    "(qualifier) \"" + key + "\" already in use by  \"" +
                    existingService.name() + "\"; cannot provide service \"" +
                    httpService.name() + "\""));
            }

            if (isShuttingDown.get()) {
                services.remove(key);
                return Future.failure(cannotProvideServiceShuttingDownException(null));
            }

            final var handle = new ServiceHandle(httpService, key);

            return pluginNotifier.onServiceProvided(httpService.description())
                .mapResult(result1 -> {
                    synchronized (handles) {
                        handles.add(handle);
                    }
                    if (result1.isSuccess() && !isShuttingDown.get()) {
                        return Result.success(handle);
                    }
                    handle.dismiss();
                    return Result.failure(cannotProvideServiceShuttingDownException(result1.fault()));
                });
        });
    }

    private Throwable cannotProvideServiceShuttingDownException(final Throwable cause) {
        return new IllegalStateException("Cannot provide service; server is shutting down", cause);
    }

    @Override
    public Collection<ArServiceHandle> providedServices() {
        synchronized (handles) {
            return Collections.unmodifiableCollection(new ArrayList<>(handles));
        }
    }

    private Optional<HttpServerService> getServiceByPath(final String path) {
        for (final var entry : services.entrySet()) {
            final var service = entry.getValue();
            if (logger.isTraceEnabled()) {
                logger.trace("Matching " + service.basePath() + " against " + path);
            }
            if (service.basePath().map(path::startsWith).orElse(true)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Matched " + service.basePath() + " of " + service);
                }
                return Optional.of(service);
            } else if (logger.isTraceEnabled()) {
                logger.trace("Failed to match " + service.basePath() + " against " + path);
            }
        }
        return Optional.empty();
    }

    @Override
    public Future<?> close() {
        if (isShuttingDown.getAndSet(true)) {
            return Future.done();
        }
        for (final var handle : handles) {
            handle.dismiss();
        }
        handles.clear();
        return adapt(channel.close());
    }

    private class ServiceHandle implements ArServiceHandle {
        private final HttpServerService httpService;
        private final AtomicBoolean isDismissed = new AtomicBoolean(false);
        private final String key;

        public ServiceHandle(final HttpServerService httpService, final String key) {
            this.httpService = httpService;
            this.key = key;
        }

        @Override
        public ServiceRecord description() {
            return httpService.description();
        }

        @Override
        public void dismiss() {
            if (!isDismissed.getAndSet(true)) {
                pluginNotifier.onServiceDismissed(description());
                services.remove(key);
                if (!isShuttingDown.get()) {
                    synchronized (handles) {
                        handles.remove(this);
                    }
                }
            }
        }

        @Override
        public boolean isDismissed() {
            return isDismissed.get();
        }
    }
}
