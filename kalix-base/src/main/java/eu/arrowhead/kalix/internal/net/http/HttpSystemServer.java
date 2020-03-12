package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.ArrowheadService;
import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.internal.SystemServer;
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
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import static eu.arrowhead.kalix.internal.util.concurrent.NettyFutures.adapt;

public class HttpSystemServer implements SystemServer {
    private final TreeMap<String, HttpService> providedServices = new TreeMap<>();
    private final ArrowheadSystem system;

    private Channel channel = null;

    public HttpSystemServer(final ArrowheadSystem system) {
        this.system = system;
    }

    @Override
    public Future<InetSocketAddress> start() {
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
        return channel != null
            ? adapt(channel.close())
            : Future.done();
    }

    @Override
    public Collection<HttpService> providedServices() {
        return providedServices.values();
    }

    private synchronized Optional<HttpService> getServiceByPath(final String path) {
        for (final var entry : providedServices.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean provideService(final ArrowheadService service) {
        Objects.requireNonNull(service, "Expected service");
        if (!(service instanceof HttpService)) {
            throw new IllegalArgumentException("Expected service to be HttpService");
        }
        final var existingService = providedServices.putIfAbsent(service.qualifier(), (HttpService) service);
        if (existingService != null) {
            if (existingService == service) {
                return false;
            }
            throw new IllegalStateException("Qualifier (base path) \"" +
                service.qualifier() + "\" already in use by  \"" +
                existingService.name() + "\"; cannot provide \"" +
                service.name() + "\"");
        }
        return true;
    }

    @Override
    public boolean dismissService(final ArrowheadService service) {
        Objects.requireNonNull(service, "Expected service");
        return providedServices.remove(service.qualifier()) != null;
    }

    @Override
    public void dismissAllServices() {
        providedServices.clear();
    }
}
