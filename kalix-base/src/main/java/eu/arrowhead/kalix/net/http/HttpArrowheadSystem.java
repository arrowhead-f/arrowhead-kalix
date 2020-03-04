package eu.arrowhead.kalix.net.http;

import eu.arrowhead.kalix.ArrowheadSystem;
import eu.arrowhead.kalix.descriptor.InterfaceDescriptor;
import eu.arrowhead.kalix.descriptor.ServiceDescriptor;
import eu.arrowhead.kalix.descriptor.TransportDescriptor;
import eu.arrowhead.kalix.internal.net.NettyBootstraps;
import eu.arrowhead.kalix.internal.util.concurrent.NettyScheduler;
import eu.arrowhead.kalix.internal.util.logging.LogLevels;
import eu.arrowhead.kalix.net.http.service.HttpService;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.concurrent.Future;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.SSLEngine;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link ArrowheadSystem} that provides {@link HttpService}s.
 */
public class HttpArrowheadSystem extends ArrowheadSystem<HttpService> {
    private final AtomicReference<InetSocketAddress> localSocketAddress = new AtomicReference<>();
    private final HashSet<HttpService> providedServices = new HashSet<>();

    // Created when requested.
    private ServiceDescriptor[] providedServiceDescriptors = null;

    private HttpArrowheadSystem(final Builder builder) {
        super(builder);
    }

    @Override
    public InetAddress localAddress() {
        final var socketAddress = localSocketAddress.get();
        return socketAddress != null
            ? socketAddress.getAddress()
            : super.localAddress();
    }

    @Override
    public int localPort() {
        final var socketAddress = localSocketAddress.get();
        return socketAddress != null
            ? socketAddress.getPort()
            : super.localPort();
    }

    @Override
    public synchronized ServiceDescriptor[] providedServices() {
        if (providedServiceDescriptors != null) {
            return providedServiceDescriptors;
        }
        final var descriptors = new ServiceDescriptor[providedServices.size()];
        var i = 0;
        for (final var service : providedServices) {
            descriptors[i++] = new ServiceDescriptor(
                service.name(),
                Stream.of(service.formats())
                    .map(format -> InterfaceDescriptor
                        .getOrCreate(TransportDescriptor.HTTP, isSecured(), format.asDescriptor()))
                    .collect(Collectors.toList()));
        }
        return providedServiceDescriptors = descriptors;
    }

    @Override
    public synchronized void provideService(final HttpService service) {
        providedServices.add(service);
        providedServiceDescriptors = null; // Force recreation.
    }

    @Override
    public synchronized void dismissService(final HttpService service) {
        providedServices.remove(service);
        providedServiceDescriptors = null; // Force recreation.
    }

    @Override
    public synchronized void dismissAllServices() {
        providedServices.clear();
        providedServiceDescriptors = null; // Force recreation.
    }

    @Override
    public Future<?> serve() {
        try {
            SslContext sslContext = null;
            {
                if (isSecured()) {
                    final var keyStore = keyStore();
                    sslContext = SslContextBuilder
                        .forServer(keyStore.privateKey(), keyStore.certificateChain())
                        .trustManager(trustStore().certificates())
                        .clientAuth(ClientAuth.REQUIRE)
                        .startTls(false)
                        .build();
                }
            }

            final var channelFuture = NettyBootstraps
                .createServerBootstrapUsing((NettyScheduler) scheduler())
                .handler(new LoggingHandler(LogLevels.toNettyLogLevel(logLevel())))
                .childHandler(new ConnectionInitializer(sslContext))
                .bind(super.localAddress(), super.localPort());

            final var isCanceled = new AtomicBoolean();
            final var future0 = new AtomicReference<>(channelFuture);
            return new Future<>() {
                @Override
                public void onResult(final Consumer<Result<Object>> consumer) {
                    channelFuture.addListener(future1 -> {
                        if (isCanceled.get()) {
                            consumer.accept(Result.failure(new CancellationException()));
                            return;
                        }
                        if (!future1.isSuccess()) {
                            consumer.accept(Result.failure(future1.cause()));
                            return;
                        }
                        final var channelFuture = (ChannelFuture) future1;

                        final var channel = channelFuture.channel();
                        localSocketAddress.set((InetSocketAddress) channel.localAddress());

                        final var closeFuture = channel.closeFuture();
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
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
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

    private static class ConnectionInitializer extends ChannelInitializer<SocketChannel> {
        private final SslContext sslContext;

        public ConnectionInitializer(final SslContext sslContext) {
            this.sslContext = sslContext;
        }

        @Override
        protected void initChannel(final SocketChannel ch) throws Exception {
            final var pipeline = ch.pipeline();
            SSLEngine sslEngine = null;
            if (sslContext != null) {
                final var handler = sslContext.newHandler(ch.alloc());
                sslEngine = handler.engine();
                pipeline.addLast(handler);
            }
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new HttpContentCompressor());
            pipeline.addLast(new HttpRequestHandler(sslEngine));
        }
    }

    private static class HttpRequestHandler extends SimpleChannelInboundHandler<Object> {
        private final SSLEngine sslEngine;

        private HttpRequest request;

        public HttpRequestHandler(final SSLEngine sslEngine) {
            this.sslEngine = sslEngine;
        }

        @Override
        protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception {
            if (msg instanceof HttpRequest) {
                handleRequest(ctx, (HttpRequest) msg);
            }
            if (msg instanceof HttpContent) {
                handleContent(ctx, (HttpContent) msg);
            }
        }

        @Override
        public void channelReadComplete(final ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
            super.channelReadComplete(ctx);
        }

        private void handleRequest(final ChannelHandlerContext ctx, final HttpRequest request) {
            this.request = request;

            if (HttpUtil.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE,
                    Unpooled.EMPTY_BUFFER
                ));
            }
        }

        private void handleContent(final ChannelHandlerContext ctx, final HttpContent content) {

        }
    }
}
