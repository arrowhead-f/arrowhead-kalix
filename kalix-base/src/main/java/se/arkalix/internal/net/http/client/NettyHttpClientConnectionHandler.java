package se.arkalix.internal.net.http.client;

import se.arkalix.internal.net.http.NettyHttpBodyReceiver;
import se.arkalix.net.http.client.HttpClientConnectionException;
import se.arkalix.net.http.client.HttpClientResponseException;
import se.arkalix.util.Result;
import se.arkalix.util.annotation.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Objects;

@Internal
public class NettyHttpClientConnectionHandler extends SimpleChannelInboundHandler<HttpObject> {
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
                    final var chain = sslHandler.engine().getSession().getPeerCertificates();
                    connection = new NettyHttpClientConnection(ctx.channel(), chain);
                    futureConnection.setResult(Result.success(connection));
                    futureConnection = null;
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
            handleResponseHead(ctx, (HttpResponse) msg);
        }
        if (msg instanceof HttpContent) {
            handleResponseContent((HttpContent) msg);
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
        if (body != null) {
            body.finish();
            body = null;
        }
    }

    private void handleResponseHead(final ChannelHandlerContext ctx, final HttpResponse response) {
        // TODO: Enable and check size restrictions.

        final var serviceResponseBody = new NettyHttpBodyReceiver(ctx.alloc(), response.headers());
        final var serviceResponse = new NettyHttpClientResponse(serviceResponseBody, response);

        this.body = serviceResponseBody;

        connection.onResponseResult(Result.success(serviceResponse));
    }

    private void handleResponseContent(final HttpContent content) {
        if (body == null) {
            return;
        }
        body.append(content);
        if (content instanceof LastHttpContent) {
            body.finish((LastHttpContent) content);
            body = null;
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
        if (connection.onResponseResult(Result.failure(cause))) {
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
                    else {
                        connection.onResponseResult(Result.failure(exception));
                    }
                }
            }
            ctx.close();
        }
    }
}
