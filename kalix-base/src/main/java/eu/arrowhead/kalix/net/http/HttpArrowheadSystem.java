package eu.arrowhead.kalix.net.http;

import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.descriptor.ServiceDescriptor;
import eu.arrowhead.kalix.internal.net.NettyBootstraps;
import eu.arrowhead.kalix.internal.util.concurrent.NettyScheduler;
import eu.arrowhead.kalix.internal.util.logging.LogLevels;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HttpArrowheadSystem extends ArrowheadSystem<HttpService> {
    private final HashSet<HttpService> providedServices = new HashSet<>();
    private final HashSet<ServiceDescriptor> providedServiceDescriptors = new HashSet<>();

    private HttpArrowheadSystem(final Builder builder) {
        super(builder);
    }

    @Override
    public synchronized Set<ServiceDescriptor> providedServices() {
        return Collections.unmodifiableSet(providedServiceDescriptors);
    }

    @Override
    public synchronized void provideService(final HttpService service) {
        if (providedServices.add(service)) {
            // TODO: Start service.
        }
    }

    @Override
    public synchronized void dismissService(final HttpService service) {
        if (providedServices.remove(service)) {
            // TODO: Stop service.
        }
    }

    @Override
    public synchronized void dismissAllServices() {
        providedServices.clear();
        providedServiceDescriptors.clear();
    }

    @Override
    public Future<?> serve() {
        final var future = NettyBootstraps
            .createServerBootstrapUsing((NettyScheduler) scheduler())
            .handler(new LoggingHandler(LogLevels.toNettyLogLevel(logLevel())))
            .childHandler(new ChildHandler())
            .bind(super.localAddress(), super.localPort());

        final var isCanceled = new AtomicBoolean();
        final var future0 = new AtomicReference<>(future);
        return new Future<>() {
            @Override
            public void onResult(final Consumer<Result<Object>> consumer) {
                future.addListener(future1 -> {
                    if (isCanceled.get()) {
                        consumer.accept(Result.failure(new CancellationException()));
                        return;
                    }
                    if (!future1.isSuccess()) {
                        consumer.accept(Result.failure(future1.cause()));
                        return;
                    }
                    final var channelFuture = (ChannelFuture) future1;
                    final var closeFuture = channelFuture.channel().closeFuture();
                    future0.set(closeFuture);
                    closeFuture.addListener(future2 ->
                        consumer.accept(future2.isSuccess()
                            ? Result.success(null)
                            : Result.failure(future2.cause())));
                });
            }

            @Override
            public void cancel(final boolean mayInterruptIfRunning) {
                isCanceled.set(true);
                future0.get().cancel(mayInterruptIfRunning);
            }
        };
    }

    public static class Builder extends ArrowheadSystem.Builder<Builder, HttpArrowheadSystem> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public HttpArrowheadSystem build() {
            return new HttpArrowheadSystem(this);
        }
    }

    public static class ChildHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(final SocketChannel ch) throws Exception {
            // TODO
        }
    }
}
