package eu.arrowhead.kalix.internal.net.http;

import eu.arrowhead.kalix.net.http.service.HttpRequester;
import eu.arrowhead.kalix.net.http.service.HttpServiceRequest;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLPeerUnverifiedException;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

import static eu.arrowhead.kalix.internal.net.http.NettyHttp.adapt;

public class NettyHttpServiceRequestHandler extends SimpleChannelInboundHandler<Object> {
    private final HttpServiceRequestHandler handler;
    private final SSLEngine sslEngine;

    private HttpRequest request;
    private NettyHttpServiceRequestBody body;

    public NettyHttpServiceRequestHandler(final HttpServiceRequestHandler handler, final SSLEngine sslEngine) {
        this.handler = handler;
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
        this.body = new NettyHttpServiceRequestBody();

        // TODO: Make sure configured size restrictions are honored by request headers.
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.CONTINUE,
                Unpooled.EMPTY_BUFFER
            ));
        }

        final var decoder = new QueryStringDecoder(request.uri());
        final var request0 = new HttpServiceRequest.Builder()
            .version(adapt(request.protocolVersion()))
            .method(adapt(request.method()))
            .headers(adapt(request.headers()))
            .path(decoder.path())
            .queryParameters(new NettyQueryParameterMap(decoder))
            .body(this.body)
            .requesterSupplier(() -> {
                try {
                    return new HttpRequester(
                        sslEngine != null
                            ? (X509Certificate) sslEngine.getSession().getPeerCertificates()[0]
                            : null,
                        (InetSocketAddress) ctx.channel().remoteAddress(),
                        request.headers().get("authorization")
                    );
                }
                catch (final SSLPeerUnverifiedException exception) {
                    throw new RuntimeException(exception);
                }
            })
            .build();

        handler.handle(request0).onResult(result -> {
            // TODO: Send response! If body hasn't been received yet, discard it!
        });
    }

    private void handleContent(final ChannelHandlerContext ctx, final HttpContent content) {

    }
}
