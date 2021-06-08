package se.arkalix.net.http.client._internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.codec.MediaType;
import se.arkalix.net._internal.NettyBodyOutgoing;
import se.arkalix.net._internal.NettySimpleChannelInboundHandler;
import se.arkalix.net.http._internal.NettyHttpConverters;
import se.arkalix.net.http._internal.NettyHttpHeaders;
import se.arkalix.util.concurrent._internal.FutureCompletion;
import se.arkalix.util.concurrent._internal.FutureCompletionUnsafe;
import se.arkalix.net.http.*;
import se.arkalix.net.http.client.HttpClientConnection;
import se.arkalix.net.http.client.HttpClientConnectionException;
import se.arkalix.net.http.client.HttpClientRequest;
import se.arkalix.net.http.client.HttpClientResponse;
import se.arkalix.security.SecurityDisabled;
import se.arkalix.util.InternalException;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import se.arkalix.util.concurrent.Future;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.security.cert.Certificate;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static se.arkalix.net.http._internal.NettyHttpConverters.convert;
import static se.arkalix.util.concurrent._internal.NettyFutures.adapt;

@Internal
public class NettyHttpClientConnection
    extends NettySimpleChannelInboundHandler<HttpObject>
    implements HttpClientConnection
{
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientConnection.class);

    private final Queue<FutureRequestResponse> requestResponseQueue = new LinkedList<>();
    private final SslHandler sslHandler;

    private Channel channel = null;
    private SSLSession sslSession = null;

    private FutureCompletion<HttpClientConnection> futureConnection;
    private NettyHttpClientResponse incomingResponse = null;

    private boolean isClosing = false;

    public NettyHttpClientConnection(
        final FutureCompletion<HttpClientConnection> futureConnection,
        final SslHandler sslHandler
    ) {
        this.futureConnection = Objects.requireNonNull(futureConnection, "futureConnection");
        this.sslHandler = sslHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
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
                    else if (cause instanceof ClosedChannelException) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Connection closed by " + channel.remoteAddress() +
                                " before TLS handshake could take place", cause);
                        }
                    }
                    else if (logger.isDebugEnabled()) {
                        logger.debug("Failed to complete TLS handshake with " +
                            channel.remoteAddress(), cause);
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
        var didRead = false;
        if (msg instanceof HttpResponse) {
            didRead = true;
            readResponse(ctx, (HttpResponse) msg);
        }
        if (msg instanceof HttpContent) {
            didRead = true;
            readContent(ctx, (HttpContent) msg);
        }
        if (!didRead && logger.isDebugEnabled()) {
            logger.debug("Unread {}", msg);
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
        incomingResponse = new NettyHttpClientResponse(ctx.alloc(), this, futureRequestResponse.request(), response);
        futureRequestResponse.complete(Result.success(incomingResponse));
    }


    private void readContent(final ChannelHandlerContext ctx, final HttpContent content) {
        if (incomingResponse == null) {
            return;
        }
        final var body = incomingResponse.body();
        if (body.isCancelled()) {
            return;
        }
        body.write(content.content());
        if (content instanceof LastHttpContent) {
            incomingResponse.unwrap().headers().add(((LastHttpContent) content).trailingHeaders());
            incomingResponse = null;
            body.close();
            if (isClosing && requestResponseQueue.isEmpty()) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (cause instanceof SSLHandshakeException) {
            if (logger.isDebugEnabled()) {
                logger.debug("SSL handshake failed", cause);
            }
            return;
        }
        if (futureConnection != null) {
            futureConnection.complete(Result.failure(cause));
            futureConnection = null;
            return;
        }
        if (incomingResponse != null) {
            final var body = incomingResponse.body();
            if (!body.isDone()) {
                body.abort(cause);
                incomingResponse = null;
                return;
            }
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
            ctx.fireUserEventTriggered(evt);
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
                final var body = incomingResponse.body();
                if (!body.isDone()) {
                    body.abort(new HttpOutgoingRequestException(incomingResponse.request(), "Incoming response body timed out"));
                    incomingResponse = null;
                    return;
                }
            }
            if (requestResponseQueue.size() > 0) {
                final var pendingResponse = requestResponseQueue.remove();
                pendingResponse.complete(Result.failure(
                    new HttpOutgoingRequestException(pendingResponse.request(), "Incoming response timed out")));
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
    public Future<HttpClientResponse> send(final HttpClientRequest request) {
        return send(request, false);
    }

    @Override
    public Future<HttpClientResponse> sendAndClose(final HttpClientRequest request) {
        return send(request, true);
    }

    private Future<HttpClientResponse> send(final HttpClientRequest request, final boolean close) {
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
            final var nettyHeaders = ((NettyHttpHeaders) request.headers()).unwrap();

            final var body = NettyBodyOutgoing.from(request.body().orElse(null), channel.alloc());

            if (!nettyHeaders.contains(CONTENT_LENGTH)) {
                nettyHeaders.set(CONTENT_LENGTH, Long.toString(body.length()));
            }

            final var host = (InetSocketAddress) channel.remoteAddress();
            nettyHeaders.set(HOST, host.getHostString() + ":" + host.getPort());

            HttpUtil.setKeepAlive(nettyHeaders, nettyVersion, !close);

            if (!nettyHeaders.contains(CONTENT_TYPE)) {
                body.codecType()
                    .ifPresent(codec -> nettyHeaders.set(CONTENT_TYPE, MediaType.getOrCreate(codec)));
            }

            channel.write(new DefaultHttpRequest(nettyVersion, nettyMethod, uri, nettyHeaders));
            channel.write(body.content());
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
        var future = adapt(channel.close());
        if (logger.isDebugEnabled()) {
            logger.debug("Closing ...");
            future = future.always(result -> logger.debug("Closed {}", result));
        }
        return future;
    }

    static class FutureRequestResponse extends FutureCompletionUnsafe<HttpClientResponse> {
        private final HttpClientRequest request;

        private FutureRequestResponse(final HttpClientRequest request) {
            this.request = request;
        }

        public HttpClientRequest request() {
            return request;
        }
    }

}