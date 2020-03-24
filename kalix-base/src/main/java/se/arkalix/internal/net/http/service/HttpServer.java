package se.arkalix.internal.net.http.service;

import se.arkalix.ArService;
import se.arkalix.ArServiceHandle;
import se.arkalix.ArSystem;
import se.arkalix.description.ServiceDescription;
import se.arkalix.internal.ArServer;
import se.arkalix.internal.net.NettyBootstraps;
import se.arkalix.internal.plugin.PluginNotifier;
import se.arkalix.net.http.service.HttpService;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;
import io.netty.channel.Channel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class HttpServer implements ArServer {
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    private final Set<ArServiceHandle> handles = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, HttpServiceInternal> services = new ConcurrentSkipListMap<>(Comparator.reverseOrder());

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
                final var keyStore = system.keyStore();
                sslContext = SslContextBuilder
                    .forServer(keyStore.privateKey(), keyStore.systemChain())
                    .trustManager(system.trustStore().certificates())
                    .clientAuth(ClientAuth.REQUIRE)
                    .startTls(false)
                    .build();
            }

            final var bootstrap = NettyBootstraps
                .createServerBootstrapUsing(system.scheduler())
                .handler(new LoggingHandler()) // TODO: Better logging/error handling
                .childHandler(new NettyHttpServiceConnectionInitializer(server::getServiceByPath, sslContext));

            return adapt(bootstrap.bind(system.localAddress(), system.localPort()))
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
    public InetSocketAddress localSocketAddress() {
        return (InetSocketAddress) channel.localAddress();
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

        if (isShuttingDown.get()) {
            return Future.failure(cannotProvideServiceShuttingDownException());
        }

        return pluginNotifier.onServicePrepared(service).flatMapResult(result0 -> {
            if (result0.isFailure()) {
                return Future.failure(result0.fault());
            }
            final var httpService = new HttpServiceInternal(system, (HttpService) service);
            final var basePath = httpService.basePath();

            final var existingService = services.putIfAbsent(basePath, httpService);
            if (existingService != null) {
                return Future.failure(new IllegalStateException("Base path " +
                    "(qualifier) \"" + basePath + "\" already in use by  \"" +
                    existingService.name() + "\"; cannot provide service \"" +
                    httpService.name() + "\""));
            }

            if (isShuttingDown.get()) {
                services.remove(basePath);
                return Future.failure(cannotProvideServiceShuttingDownException());
            }

            final var handle = new ServiceHandle(httpService, basePath);

            return pluginNotifier.onServiceProvided(httpService.description())
                .mapResult(result1 -> {
                    handles.add(handle);
                    if (result1.isSuccess() && !isShuttingDown.get()) {
                        return Result.success(handle);
                    }
                    handle.dismiss();
                    return Result.failure(cannotProvideServiceShuttingDownException());
                });
        });
    }

    private Throwable cannotProvideServiceShuttingDownException() {
        return new IllegalStateException("Cannot provide service; server is shutting down");
    }

    @Override
    public Stream<ArServiceHandle> providedServices() {
        return handles.stream();
    }

    private Optional<HttpServiceInternal> getServiceByPath(final String path) {
        for (final var entry : services.entrySet()) {
            final var service = entry.getValue();
            if (path.startsWith(service.basePath())) {
                return Optional.of(service);
            }
        }
        return Optional.empty();
    }

    @Override
    public Future<?> close() {
        if (isShuttingDown.get()) {
            return Future.done();
        }
        for (final var handle : handles) {
            handle.dismiss();
        }
        return adapt(channel.close());
    }

    private class ServiceHandle implements ArServiceHandle {
        private final HttpServiceInternal httpService;
        private final AtomicBoolean isDismissed = new AtomicBoolean(false);
        private final String basePath;

        public ServiceHandle(final HttpServiceInternal httpService, final String basePath) {
            this.httpService = httpService;
            this.basePath = basePath;
        }

        @Override
        public ServiceDescription description() {
            return httpService.description();
        }

        @Override
        public void dismiss() {
            if (isDismissed.compareAndSet(false, true)) {
                pluginNotifier.onServiceDismissed(description());
                services.remove(basePath);
            }
        }

        @Override
        public boolean isDismissed() {
            return isDismissed.get();
        }
    }
}
