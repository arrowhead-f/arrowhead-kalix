package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.internal.ArrowheadServer;
import eu.arrowhead.kalix.internal.net.NettyBootstraps;
import eu.arrowhead.kalix.internal.net.http.service.NettyHttpServiceConnectionInitializer;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.channel.Channel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

public class HttpArrowheadServer extends ArrowheadServer {
    private final Map<String, HttpService> providedServices = new ConcurrentSkipListMap<>();

    private Channel channel = null;

    public HttpArrowheadServer(final ArrowheadSystem system) {
        super(system);
    }

    @Override
    public Future<InetSocketAddress> start() {
        try {
            final var system = system();
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
            return adapt(NettyBootstraps
                .createServerBootstrapUsing(system.scheduler())
                .handler(new LoggingHandler())
                .childHandler(new NettyHttpServiceConnectionInitializer(this::getServiceByPath, sslContext))
                .bind(system.localAddress(), system.localPort()))
                .map(channel0 -> {
                    channel = channel0;
                    return (InetSocketAddress) channel0.localAddress();
                });
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<?> stop() {
        providedServices.clear();
        return channel != null
            ? adapt(channel.close())
            : Future.done();
    }

    @Override
    public boolean canProvide(final ArrowheadService service) {
        return service instanceof HttpService;
    }

    @Override
    public synchronized Collection<HttpService> providedServices() {
        return Collections.unmodifiableCollection(providedServices.values());
    }

    private Optional<HttpService> getServiceByPath(final String path) {
        for (final var entry : providedServices.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public void provideService(final ArrowheadService service) {
        Objects.requireNonNull(service, "Expected service");
        if (!(service instanceof HttpService)) {
            throw new IllegalArgumentException("Expected service to be HttpService");
        }
        final var existingService = providedServices.putIfAbsent(service.qualifier(), (HttpService) service);
        if (existingService != null && existingService != service) {
            throw new IllegalStateException("Qualifier (base path) \"" +
                service.qualifier() + "\" already in use by  \"" +
                existingService.name() + "\"; cannot provide \"" +
                service.name() + "\"");
        }
    }

    @Override
    public void dismissService(final ArrowheadService service) {
        Objects.requireNonNull(service, "Expected service");
        providedServices.remove(service.qualifier());
    }
}
