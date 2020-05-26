package se.arkalix.internal.net.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.arkalix.internal.net.http.NettyHttpBodyReceiver;
import se.arkalix.internal.net.NettySimpleChannelInboundHandler;
import se.arkalix.net.http.client.HttpClientConnectionException;
import se.arkalix.net.http.client.HttpClientResponseException;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Objects;

@Internal
public class NettyHttpClientConnectionHandler extends NettySimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = LoggerFactory.getLogger(NettyHttpClientConnectionHandler.class);

    private final SslHandler sslHandler;

    private FutureHttpClientConnection futureConnection;
    private NettyHttpClientConnection connection;
    private NettyHttpBodyReceiver body = null;

    public NettyHttpClientConnectionHandler(
        final FutureHttpClientConnection futureConnection,
        final SslHandler sslHandler)
    {
        this.futureConnection = Objects.requireNonNull(futureConnection, "Expected connection");
        this.sslHandler = sslHandler;
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        if (futureConnection != null) {
            if (futureConnection.failIfCancelled()) {
                futureConnection = null;
                ctx.close();
                return;
            }
            if (sslHandler != null) {
                sslHandler.handshakeFuture().addListener(ignored -> {
                    try {
                        final var chain = sslHandler.engine().getSession().getPeerCertificates();
                        connection = new NettyHttpClientConnection(ctx.channel(), chain);
                        futureConnection.setResult(Result.success(connection));
                        futureConnection = null;
                    }
                    catch (final Throwable throwable) {
                        if (futureConnection != null) {
                            futureConnection.setResult(Result.failure(throwable));
                            futureConnection = null;
                        }
                        else {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Failed to complete TLS handshake with remote host", throwable);
                            }
                        }
                        ctx.close();
                    }
                });
            }
            else {
                connection = new NettyHttpClientConnection(ctx.channel(), null);
                futureConnection.setResult(Result.success(connection));
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
            readContent(ctx,(HttpContent) msg);
        }
    }

    private void readResponse(final ChannelHandlerContext ctx, final HttpResponse response) {
        // TODO: Enable and check size restrictions.

        final var clientResponseBody = new NettyHttpBodyReceiver(ctx.alloc(), response.headers());
        final var clientResponse = new NettyHttpClientResponse(clientResponseBody, response);

        this.body = clientResponseBody;

        connection.onResponseResult(Result.success(clientResponse));
    }

    private void readContent(final ChannelHandlerContext ctx, final HttpContent content) {
        if (body == null) {
            return;
        }
        body.append(content);
        if (content instanceof LastHttpContent) {
            body.finish((LastHttpContent) content);
            body = null;
            if (connection != null && connection.isClosing()) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (futureConnection != null) {
            futureConnection.setResult(Result.failure(cause));
            futureConnection = null;
            return;
        }
        if (body != null && body.tryAbort(cause)) {
            body = null;
            return;
        }
        if (connection != null && connection.onResponseResult(Result.failure(cause))) {
            return;
        }
        ctx.fireExceptionCaught(cause);
    }

    @Override
    public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) {
        if (evt instanceof IdleStateEvent) {
            final var idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                if (futureConnection != null) {
                    futureConnection.setResult(Result.failure(new HttpClientConnectionException("Timeout exceeded")));
                }
                else {
                    final var exception = new HttpClientResponseException("Incoming response body timed out");
                    if (body != null) {
                        body.tryAbort(exception);
                        body = null;
                    }
                    else if (connection != null) {
                        connection.onResponseResult(Result.failure(exception));
                    }
                }
            }
            ctx.close();
        }
    }
}
