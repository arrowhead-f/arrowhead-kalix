package eu.arrowhead.kalix.internal.net.http.service;

import eu.arrowhead.kalix.AhfService;
import eu.arrowhead.kalix.AhfServiceHandle;
import eu.arrowhead.kalix.AhfSystem;
import eu.arrowhead.kalix.description.ServiceDescription;
import eu.arrowhead.kalix.internal.AhfServer;
import eu.arrowhead.kalix.internal.net.NettyBootstraps;
import eu.arrowhead.kalix.internal.plugin.PluginNotifier;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.annotation.Internal;
import eu.arrowhead.kalix.util.concurrent.Future;
import eu.arrowhead.kalix.util.concurrent.Futures;
import io.netty.channel.Channel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class HttpServer implements AhfServer {
    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTING = 1;
    private static final int STATE_STARTED = 2;
    private static final int STATE_STOPPING = 3;
    private static final int STATE_SHUTTING_DOWN = 4;

    private final AtomicInteger state = new AtomicInteger(STATE_STOPPED);

    private final Set<AhfServiceHandle> handles = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, HttpServiceInternal> services = new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    private final AhfSystem system;
    private final PluginNotifier pluginNotifier;

    private Channel channel = null;

    public HttpServer(final AhfSystem system, final PluginNotifier pluginNotifier) {
        this.system = Objects.requireNonNull(system, "Expected system");
        this.pluginNotifier = Objects.requireNonNull(pluginNotifier, "Expected pluginNotifier");
    }

    @Override
    public boolean canProvide(final AhfService service) {
        return service instanceof HttpService;
    }

    @Override
    public Future<AhfServiceHandle> provide(final AhfService service) {
        Objects.requireNonNull(service, "Expected service");

        if (!(service instanceof HttpService)) {
            throw new IllegalArgumentException("Expected service to be HttpService");
        }

        switch (state.get()) {
        case STATE_STOPPED:
        case STATE_STARTING:
        case STATE_STARTED:
        case STATE_STOPPING:
            break;

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Cannot provide service; server is shutting down"));

        default:
            return Future.failure(new IllegalStateException("Server in illegal state: " + state.get()));
        }

        return pluginNotifier.onServicePrepared(service).flatMapResult(result0 -> {
            if (result0.isFailure()) {
                return Future.failure(result0.fault());
            }
            final var httpService = new HttpServiceInternal((HttpService) service);
            final var basePath = httpService.basePath();

            final var existingService = services.putIfAbsent(basePath, httpService);
            if (existingService != null) {
                return Future.failure(new IllegalStateException("Base path " +
                    "(qualifier) \"" + basePath + "\" already in use by  \"" +
                    existingService.name() + "\"; cannot provide service \"" +
                    httpService.name() + "\""));
            }

            if (state.get() == STATE_SHUTTING_DOWN) {
                services.remove(basePath);
                return Future.failure(new IllegalStateException("Failed to provide service; server is shutting down"));
            }

            final var handle = new ServiceHandle(httpService, basePath);

            return pluginNotifier.onServiceProvided(httpService.describe())
                .flatMapResult(result1 -> {
                    handles.add(handle);
                    if (result1.isSuccess() && state.get() != STATE_SHUTTING_DOWN) {
                        return Future.success(handle);
                    }
                    return handle.dismiss()
                        .map(ignored -> null);
                });
        });
    }

    @Override
    public Stream<AhfServiceHandle> providedServices() {
        return handles.stream();
    }

    @Override
    public Future<InetSocketAddress> start() {
        switch (state.compareAndExchange(STATE_STOPPED, STATE_STARTING)) {
        case STATE_STOPPED:
            break;

        case STATE_STARTING:
            return Future.failure(new IllegalStateException("Already starting"));

        case STATE_STARTED:
            return Future.success((InetSocketAddress) channel.localAddress());

        case STATE_STOPPING:
            return Future.failure(new IllegalStateException("Cannot start while stopping"));

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Cannot start while shutting down"));

        default:
            return Future.failure(new IllegalStateException("Server in illegal state: " + state.get()));
        }

        try {
            SslContext sslContext = null;
            if (system.isSecure()) {
                final var keyStore = system.keyStore();
                sslContext = SslContextBuilder
                    .forServer(keyStore.privateKey(), keyStore.certificateChain())
                    .trustManager(system.trustStore().certificates())
                    .clientAuth(ClientAuth.REQUIRE)
                    .startTls(false)
                    .build();
            }

            final var channelFuture = NettyBootstraps
                .createServerBootstrapUsing(system.scheduler())
                .handler(new LoggingHandler()) // TODO: Better logging/error handling
                .childHandler(new NettyHttpServiceConnectionInitializer(this::getServiceByPath, sslContext))
                .bind(system.localAddress(), system.localPort());

            return adapt(channelFuture).flatMapResult(result -> {
                if (result.isSuccess()) {
                    channel = result.value();
                    if (state.compareAndExchange(STATE_STARTING, STATE_STARTED) == STATE_SHUTTING_DOWN) {
                        return adapt(channel.close())
                            .mapResult(result1 -> Result.failure(result1.isSuccess()
                                ? new CancellationException("Server is shutting down")
                                : result1.fault()));
                    }
                    return Future.success((InetSocketAddress) channel.localAddress());
                }
                state.compareAndSet(STATE_STARTING, STATE_STOPPED);
                return Future.failure(result.fault());
            });
        }
        catch (final Throwable throwable) {
            state.compareAndSet(STATE_STARTING, STATE_STOPPED);
            return Future.failure(throwable);
        }
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
            return Future.failure(new IllegalStateException("Server in illegal state: " + state.get()));
        }
        return adapt(channel.close())
            .mapResult(result -> {
                state.compareAndSet(STATE_STOPPING, STATE_STOPPED);
                return result;
            });
    }

    @Override
    public Future<?> shutdown() {
        final var lastState = state.getAndSet(STATE_SHUTTING_DOWN);
        final boolean isStarted;
        switch (lastState) {
        case STATE_STOPPED:
        case STATE_STOPPING:
        case STATE_STARTING:
            isStarted = false;
            break;

        case STATE_STARTED:
            isStarted = true;
            break;

        case STATE_SHUTTING_DOWN:
            return Future.failure(new IllegalStateException("Already shutting down"));

        default:
            return Future.failure(new IllegalStateException("Server in illegal state: " + state.get()));
        }

        return (isStarted ? adapt(channel.close()) : Future.done())
            .flatMap(ignored -> Futures.serialize(handles.stream().map(AhfServiceHandle::dismiss).iterator()));
    }

    private class ServiceHandle implements AhfServiceHandle {
        private final HttpServiceInternal httpService;
        private final AtomicBoolean isDismissed = new AtomicBoolean(false);
        private final String basePath;

        public ServiceHandle(final HttpServiceInternal httpService, final String basePath) {
            this.httpService = httpService;
            this.basePath = basePath;
        }

        @Override
        public ServiceDescription description() {
            return httpService.describe();
        }

        @Override
        public Future<?> dismiss() {
            if (isDismissed.compareAndSet(false, true)) {
                return pluginNotifier.onServiceDismissed(description())
                    .map(ignored -> {
                        services.remove(basePath);
                        handles.remove(this);
                        return null;
                    });
            }
            return Future.done();
        }
    }
}
