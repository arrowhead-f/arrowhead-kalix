package eu.arrowhead.kalix.internal.net.http.client;

import eu.arrowhead.kalix.internal.net.http.NettyHttpBodyReceiver;
import eu.arrowhead.kalix.util.Result;
import eu.arrowhead.kalix.util.annotation.Internal;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;

import java.security.cert.X509Certificate;
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
                    final var x509chain = new X509Certificate[chain.length];
                    for (var i = 0; i < chain.length; ++i) {
                        final var certificate = chain[i];
                        if (!(certificate instanceof X509Certificate)) {
                            futureConnection.setResult(Result.failure(new IllegalStateException("" +
                                "Only x.509 certificates may be used by " +
                                "remote peers, somehow the peer at " +
                                ctx.channel().remoteAddress() + " was able " +
                                "to use some other type: " + certificate)));
                            futureConnection = null;
                            ctx.close();
                            return;
                        }
                        x509chain[i] = (X509Certificate) chain[i];
                    }
                    connection = new NettyHttpClientConnection(ctx.channel(), x509chain);
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
        }
    }

    // TODO: Bring any response exceptions back to client.

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        if (futureConnection != null) {
            futureConnection.setResult(Result.failure(cause));
            return;
        }
        if (body != null) {
            body.abort(cause);
            return;
        }
        if (connection.onResponseResult(Result.failure(cause))) {
            return;
        }
        ctx.fireExceptionCaught(cause);
    }
}
