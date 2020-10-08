package se.arkalix.internal.net.http.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.internal.net.NettyBodyOutgoing;
import se.arkalix.internal.net.NettySimpleChannelInboundHandler;
import se.arkalix.internal.net.http.HttpMediaTypes;
import se.arkalix.internal.net.http.NettyHttpConverters;
import se.arkalix.internal.util.concurrent.FutureCompletion;
import se.arkalix.internal.util.concurrent.FutureCompletionUnsafe;
import se.arkalix.net.http.*;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.net.http.client.HttpClientConnectionException;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.InternalException;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;
import static se.arkalix.internal.net.http.NettyHttpConverters.convert;
import static se.arkalix.internal.util.concurrent.NettyFutures.adapt;

@Internal
public class NettyHttpClientConnection
    extends NettySimpleChannelInboundHandler<HttpObject>
    implements HttpClientConnection
{
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientConnection.class);

    private final Queue<FutureRequestResponse> requestResponseQueue = new LinkedList<>();
    private final Channel channel;
    private final SslHandler sslHandler;

    private SSLSession sslSession = null;

    private FutureCompletion<HttpClientConnection> futureConnection;
    private NettyHttpClientResponse incomingResponse = null;

    private boolean isClosing = false;

    public NettyHttpClientConnection(
        final FutureCompletion<HttpClientConnection> futureConnection,
        final Channel channel,
        final SslHandler sslHandler
    ) {
        this.futureConnection = Objects.requireNonNull(futureConnection, "Expected futureConnection");
        this.channel = Objects.requireNonNull(channel, "Expected channel");
        this.sslHandler = sslHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (futureConnection != null) {
            if (futureConnection.isCancelled()) {
                futureConnection = null;
                ctx.close();
                return;
            }
            if (sslHandler != null) {
                sslHandler.handshakeFuture().addListener(future -> {
                    Throwable cause;
                    try {
                        if (future.isSuccess()) {
                            sslSession = sslHandler.engine().getSession();
                            futureConnection.complete(Result.success(this));
                            futureConnection = null;
                            return;
                        }
                        else {
                            cause = future.cause();
                        }
                    }
                    catch (final Throwable throwable) {
                        cause = throwable;
                    }
                    if (futureConnection != null) {
                        futureConnection.complete(Result.failure(cause));
                        futureConnection = null;
                    }
                    else {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Failed to complete TLS handshake with remote host", cause);
                        }
                    }
                    ctx.close();
                });
            }
            else {
                futureConnection.complete(Result.success(this));
                futureConnection = null;
            }
        }
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject msg) {
        if (msg instanceof HttpResponse) {
            readResponse(ctx, (HttpResponse) msg);
        }
        if (msg instanceof HttpContent) {
            readContent(ctx, (HttpContent) msg);
        }
    }

    private void readResponse(final ChannelHandlerContext ctx, final HttpResponse response) {
        // TODO: Enable and check size restrictions.

        final var futureRequestResponse = requestResponseQueue.poll();
        if (futureRequestResponse == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpectedly received incoming HTTP response " +
                    "with status {} from {}", response.status(), remoteSocketAddress());
            }
            return;
        }
        incomingResponse = new NettyHttpClientResponse(ctx.alloc(), futureRequestResponse.request(), response);
        futureRequestResponse.complete(Result.success(incomingResponse));
    }


    private void readContent(final ChannelHandlerContext ctx, final HttpContent content) {
        if (incomingResponse == null) {
            return;
        }
        incomingResponse.append(content);
        if (content instanceof LastHttpContent) {
            incomingResponse.headers().unwrap().add(((LastHttpContent) content).trailingHeaders());
            incomingResponse.finish();
            incomingResponse = null;
            if (isClosing && requestResponseQueue.isEmpty()) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (futureConnection != null) {
            futureConnection.complete(Result.failure(cause));
            futureConnection = null;
            return;
        }
        if (incomingResponse != null && incomingResponse.tryAbort(cause)) {
            incomingResponse = null;
            return;
        }
        if (requestResponseQueue.size() > 0) {
            requestResponseQueue.remove().complete(Result.failure(cause));
            return;
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        try {
            final var idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() != IdleState.READER_IDLE) {
                return;
            }
            if (futureConnection != null) {
                futureConnection.complete(Result.failure(new HttpClientConnectionException("Timeout exceeded")));
                futureConnection = null;
                return;
            }
            if (incomingResponse != null) {
                incomingResponse.tryAbort(new HttpOutgoingRequestException(incomingResponse.request(), "Incoming response body timed out"));
                incomingResponse = null;
                return;
            }
            if (requestResponseQueue.size() > 0) {
                final var pendingResponse = requestResponseQueue.remove();
                pendingResponse.complete(Result.failure(
                    new HttpOutgoingRequestException(incomingResponse.request(), "Incoming response timed out")));
            }
        }
        finally {
            ctx.close();
        }
    }

    @Override
    public Certificate[] remoteCertificateChain() {
        if (sslHandler == null) {
            throw new SecurityDisabled("Not running in secure mode; remote certificate chain not available");
        }
        if (sslSession == null) {
            throw new InternalException("remoteCertificateChain() called before SSL handshake completed");
        }
        try {
            return sslSession.getPeerCertificates();
        }
        catch (final SSLPeerUnverifiedException exception) {
            throw new InternalException("remoteCertificateChain() called before SSL handshake completed", exception);
        }
    }

    @Override
    public InetSocketAddress remoteSocketAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public Certificate[] localCertificateChain() {
        if (sslHandler == null) {
            throw new SecurityDisabled("Not running in secure mode; local certificate chain not available");
        }
        if (sslSession == null) {
            throw new InternalException("localCertificateChain() called before SSL handshake completed");
        }
        try {
            return sslSession.getPeerCertificates();
        }
        catch (final SSLPeerUnverifiedException exception) {
            throw new InternalException("localCertificateChain() called before SSL handshake completed", exception);
        }
    }

    @Override
    public InetSocketAddress localSocketAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public boolean isLive() {
        return channel.isActive();
    }

    @Override
    public boolean isSecure() {
        return sslHandler != null;
    }

    @Override
    public Future<HttpIncomingResponse> send(final HttpClientRequest request) {
        return send(request, false);
    }

    @Override
    public Future<HttpIncomingResponse> sendAndClose(final HttpClientRequest request) {
        return send(request, true);
    }

    private Future<HttpIncomingResponse> send(final HttpClientRequest request, final boolean close) {
        try {
            if (isClosing) {
                throw new HttpOutgoingRequestException(request, "Client is closing; cannot send request");
            }
            isClosing = close;

            final var method = request.method()
                .orElseThrow(() -> new IllegalArgumentException("Expected method in client request"));

            final var path = request.path()
                .orElseThrow(() -> new IllegalArgumentException("Expected path in client request"));

            final var queryStringEncoder = new QueryStringEncoder(path);
            for (final var entry : request.queryParameters().entrySet()) {
                final var name = entry.getKey();
                for (final var value : entry.getValue()) {
                    queryStringEncoder.addParam(name, value);
                }
            }
            final var uri = queryStringEncoder.toString();

            final var nettyVersion = request.version()
                .map(NettyHttpConverters::convert)
                .orElse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1);
            final var nettyMethod = convert(method);
            final var nettyHeaders = request.headers().unwrap();

            final var host = (InetSocketAddress) channel.remoteAddress();
            nettyHeaders.set(HOST, host.getHostString() + ":" + host.getPort());

            HttpUtil.setKeepAlive(nettyHeaders, nettyVersion, !close);

            if (!nettyHeaders.contains(CONTENT_TYPE)) {
                final var encoding = request.encoding().orElse(null);
                if (encoding == null) {
                    nettyHeaders.set(CONTENT_TYPE, TEXT_PLAIN + ";charset=" + request.charset()
                        .orElse(StandardCharsets.UTF_8)
                        .name()
                        .toLowerCase());
                }
                else {
                    nettyHeaders.set(CONTENT_TYPE, HttpMediaTypes.toMediaType(encoding));
                }
            }

            final var body = NettyBodyOutgoing.from(request, channel.alloc(), null);

            if (!nettyHeaders.contains(CONTENT_LENGTH)) {
                nettyHeaders.set(CONTENT_LENGTH, Long.toString(body.length()));
            }

            channel.write(new DefaultHttpRequest(nettyVersion, nettyMethod, uri, nettyHeaders));
            body.writeTo(channel);
            channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            final var futureRequestResponse = new FutureRequestResponse(request);
            requestResponseQueue.add(futureRequestResponse);
            return futureRequestResponse;
        }
        catch (final Throwable throwable) {
            return Future.failure(throwable);
        }
    }

    @Override
    public Future<?> close() {
        return adapt(channel.close());
    }

    static class FutureRequestResponse extends FutureCompletionUnsafe<HttpIncomingResponse> {
        private final HttpOutgoingRequest<?> request;

        private FutureRequestResponse(final HttpOutgoingRequest<?> request) {
            this.request = request;
        }

        public HttpOutgoingRequest<?> request() {
            return request;
        }
    }

}